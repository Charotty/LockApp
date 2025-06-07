package com.example.lockapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class ModeSelectActivity extends Activity {

    // Wi-Fi константы
    private static final int WIFI_ENABLE_REQUEST = 1001;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    // Bluetooth константы
    private static final int BLUETOOTH_PERMISSION_REQUEST = 2001;
    private static final int BLUETOOTH_ENABLE_REQUEST = 2002;

    // Настройки SSID и Bluetooth (будут храниться в SharedPreferences)
    private static final String PREFS = "lockapp_prefs";
    private static final String KEY_SSID = "arduino_wifi_ssid";
    private static final String KEY_BTNAME = "arduino_bluetooth_name";
    private String arduinoWifiSsid;
    private String arduinoBtName;

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> availableDevices = new ArrayList<>();
    private BroadcastReceiver bluetoothReceiver;
    private BluetoothSocket bluetoothSocket;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String WIFI_MODULE_URL = "http://192.168.4.1"; // Arduino Wi-Fi module IP
    private final OkHttpClient simpleClient = new OkHttpClient();

    // --- WiFi Arduino connection state ---
    private String arduinoIp = null;
    private int arduinoPort = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        Button buttonWifi = findViewById(R.id.buttonWifi);
        Button buttonBluetooth = findViewById(R.id.buttonBluetooth);
        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        Button buttonEventLog = findViewById(R.id.buttonEventLog);
        Button buttonLockWifi = findViewById(R.id.buttonLockWifi);
        Button buttonLockBluetooth = findViewById(R.id.buttonLockBluetooth);
        Button buttonSettings = findViewById(R.id.buttonSettings);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Загрузка настроек
        loadSettings();

        buttonWifi.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(intent, WIFI_ENABLE_REQUEST);
        });
        buttonBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });
        buttonAdmin.setOnClickListener(v -> startActivity(new Intent(ModeSelectActivity.this, AdminTableActivity.class)));
        buttonEventLog.setOnClickListener(v -> startActivity(new Intent(ModeSelectActivity.this, EventLogActivity.class)));
        buttonLockWifi.setOnClickListener(v -> {
            if (!isConnectedToArduinoWifi()) {
                showToast("Сначала подключитесь к WiFi замка через 'Вход по WiFi'");
            } else {
                showLockControlDialogWifi();
            }
        });
        buttonLockBluetooth.setOnClickListener(v -> {
            if (!isConnectedToArduinoBluetooth()) {
                showToast("Сначала подключитесь к Bluetooth замка через 'Вход по Bluetooth'");
            } else {
                showLockControlDialogBluetooth();
            }
        });
        buttonSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private boolean isConnectedToArduinoWifi() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID();
            // Убираем кавычки, если есть
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return arduinoWifiSsid.equals(ssid);
        }
        return false;
    }

    private boolean isConnectedToArduinoBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
            if (paired != null) {
                for (BluetoothDevice device : paired) {
                    if (arduinoBtName.equals(device.getName())) {
                        // Можно добавить проверку на активное соединение, если реализовано
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isConnectedToArduinoWifi()) {
            showToast("Устройство подключено к замку по WiFi");
        }
        if (isConnectedToArduinoBluetooth()) {
            showToast("Устройство подключено к замку по Bluetooth");
        }
    }

    // ================= Wi-Fi Функционал =================
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            handleWifiAuthentication();
        }
    }

    private void handleWifiAuthentication() {
        if (wifiManager == null) {
            showToast("Wi-Fi недоступен");
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            showEnableWifiDialog();
        } else {
            scanAvailableNetworks();
        }
    }

    private void showEnableWifiDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Включение Wi-Fi")
                .setMessage("Для подключения необходимо включить Wi-Fi. Включить сейчас?")
                .setPositiveButton("Да", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startActivityForResult(
                                new Intent(Settings.Panel.ACTION_WIFI),
                                WIFI_ENABLE_REQUEST
                        );
                    } else {
                        wifiManager.setWifiEnabled(true);
                        new Handler().postDelayed(this::scanAvailableNetworks, 2000);
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void scanAvailableNetworks() {
        if (!wifiManager.isWifiEnabled()) {
            showToast("Wi-Fi выключен");
            return;
        }

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                unregisterReceiver(this);
                try {
                    List<ScanResult> results = wifiManager.getScanResults();
                    handleAvailableNetworks(results);
                } catch (SecurityException e) {
                    showToast("Ошибка доступа: " + e.getMessage());
                }
            }
        };

        registerReceiver(
                wifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );

        if (!wifiManager.startScan()) {
            showToast("Ошибка сканирования");
        }
    }

    private void handleAvailableNetworks(List<ScanResult> results) {
        List<ScanResult> availableNetworks = new ArrayList<>();
        for (ScanResult result : results) {
            if (result.SSID != null &&
                    result.SSID.startsWith("MyCompany") &&
                    (result.capabilities.contains("WPA2") || result.capabilities.contains("WPA3"))) {
                availableNetworks.add(result);
            }
        }

        if (availableNetworks.isEmpty()) {
            showToast("Доступные сети не найдены");
        } else {
            showNetworkSelectionDialog(availableNetworks);
        }
    }

    private void showNetworkSelectionDialog(List<ScanResult> networks) {
        CharSequence[] items = new CharSequence[networks.size()];
        for (int i = 0; i < networks.size(); i++) {
            items[i] = networks.get(i).SSID;
        }

        new AlertDialog.Builder(this)
                .setTitle("Выберите сеть")
                .setItems(items, (dialog, which) -> connectToNetwork(networks.get(which)))
                .show();
    }

    private void connectToNetwork(ScanResult network) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + network.SSID + "\"";
        config.preSharedKey = "\"company_password\"";

        int networkId = wifiManager.addNetwork(config);
        if (networkId == -1) {
            showToast("Ошибка конфигурации");
            return;
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
        showToast("Подключение к " + network.SSID);

        new Handler().postDelayed(() -> {
            if (isConnectedToTargetNetwork(network.SSID)) {
                showLockControlDialogWifi();
            } else {
                showToast("Ошибка подключения");
            }
        }, 5000);
    }

    private boolean isConnectedToTargetNetwork(String targetSSID) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String connectedSSID = wifiInfo.getSSID().replace("\"", "");
        return connectedSSID.equals(targetSSID);
    }

    // ================= Bluetooth Функционал =================
    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_PERMISSION_REQUEST);
        } else {
            handleBluetoothAuthentication();
        }
    }

    private void handleBluetoothAuthentication() {
        if (bluetoothAdapter == null) {
            showToast("Bluetooth не поддерживается"); return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Включение Bluetooth")
                    .setMessage("Для продолжения необходимо включить Bluetooth. Включить сейчас?")
                    .setPositiveButton("Да", (d,w) -> {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, BLUETOOTH_ENABLE_REQUEST);
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        } else {
            scanBluetoothDevices();
        }
    }

    private void scanBluetoothDevices() {
        availableDevices.clear();
        if (bluetoothReceiver != null) unregisterReceiver(bluetoothReceiver);
        bluetoothReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) availableDevices.add(device);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    unregisterReceiver(this);
                    showBluetoothDevicesDialog();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        bluetoothAdapter.startDiscovery();
        showToast("Сканирование Bluetooth...");
    }

    private void showBluetoothDevicesDialog() {
        if (availableDevices.isEmpty()) {
            showToast("Устройства не найдены"); return;
        }
        CharSequence[] items = new CharSequence[availableDevices.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = availableDevices.get(i).getName() + " (" + availableDevices.get(i).getAddress() + ")";
        }
        new AlertDialog.Builder(this)
                .setTitle("Выберите устройство Bluetooth")
                .setItems(items, (d, which) -> connectToBluetoothDevice(availableDevices.get(which)))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                bluetoothSocket.connect();
                runOnUiThread(this::showLockControlDialogBluetooth);
            } catch (Exception e) {
                runOnUiThread(() -> showToast("Ошибка подключения Bluetooth: " + e.getMessage()));
            }
        }).start();
    }

    private void showLockControlDialogBluetooth() {
        String[] actions = {"Открыть замок", "Закрыть замок"};
        new AlertDialog.Builder(this)
                .setTitle("Управление замком (Bluetooth)")
                .setItems(actions, (d, which) -> {
                    String cmd = which == 0 ? "open" : "close";
                    sendBluetoothCommand(cmd);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void sendBluetoothCommand(String command) {
        new Thread(() -> {
            try {
                OutputStream out = bluetoothSocket.getOutputStream();
                out.write((command + "\n").getBytes());
                out.flush();
                runOnUiThread(() -> showToast("Команда отправлена: " + command));
            } catch (Exception e) {
                runOnUiThread(() -> showToast("Ошибка отправки Bluetooth: " + e.getMessage()));
            }
        }).start();
    }

    // ================= Общие методы =================
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Диалог для подключения к Arduino по WiFi
    private void showWifiConnectDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        final android.widget.EditText editIp = new android.widget.EditText(this);
        editIp.setHint("IP Arduino (например, 192.168.1.100)");
        final android.widget.EditText editPort = new android.widget.EditText(this);
        editPort.setHint("Порт (например, 8888)");
        editPort.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(editIp);
        layout.addView(editPort);
        new AlertDialog.Builder(this)
                .setTitle("Вход по WiFi")
                .setView(layout)
                .setPositiveButton("Подключиться", (dialog, whichBtn) -> {
                    String ip = editIp.getText().toString().trim();
                    String portStr = editPort.getText().toString().trim();
                    if (ip.isEmpty() || portStr.isEmpty()) {
                        showToast("Введите IP и порт");
                        return;
                    }
                    int port;
                    try {
                        port = Integer.parseInt(portStr);
                    } catch (NumberFormatException e) {
                        showToast("Некорректный порт");
                        return;
                    }
                    // Пробуем подключиться к Arduino
                    testWifiConnection(ip, port);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Проверка подключения к Arduino по WiFi (без отдельной Activity)
    private void testWifiConnection(String ip, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(ip, port), 3000);
                runOnUiThread(() -> {
                    arduinoIp = ip;
                    arduinoPort = port;
                    showToast("Успешно подключено к Arduino по WiFi");
                });
            } catch (Exception e) {
                runOnUiThread(() -> showToast("Ошибка подключения: " + e.getMessage()));
            }
        }).start();
    }

    // Диалог управления замком по WiFi (без ввода IP/порта)
    private void showLockControlDialogWifi() {
        String[] actions = {"Открыть замок", "Закрыть замок"};
        new AlertDialog.Builder(this)
                .setTitle("Управление замком (WiFi)")
                .setItems(actions, (d, which) -> {
                    String cmd = which == 0 ? "open" : "close";
                    sendWifiCommand(cmd);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Отправка команды на Arduino по WiFi (использует сохранённые ip/port)
    private void sendWifiCommand(String cmd) {
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(arduinoIp, arduinoPort), 3000);
                OutputStream out = socket.getOutputStream();
                out.write((cmd + "\n").getBytes());
                out.flush();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                runOnUiThread(() -> showToast("Ответ: " + response));
            } catch (Exception e) {
                runOnUiThread(() -> showToast("Ошибка WiFi: " + e.getMessage()));
            }
        }).start();
    }

    private void loadSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        arduinoWifiSsid = prefs.getString(KEY_SSID, "ArduinoLock");
        arduinoBtName = prefs.getString(KEY_BTNAME, "ArduinoBT");
    }

    private void saveSettings(String ssid, String btName) {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_SSID, ssid).putString(KEY_BTNAME, btName).apply();
        arduinoWifiSsid = ssid;
        arduinoBtName = btName;
    }

    private void showSettingsDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        final android.widget.EditText editSsid = new android.widget.EditText(this);
        editSsid.setHint("SSID WiFi Arduino");
        editSsid.setText(arduinoWifiSsid);
        final android.widget.EditText editBt = new android.widget.EditText(this);
        editBt.setHint("Имя Bluetooth Arduino");
        editBt.setText(arduinoBtName);
        layout.addView(editSsid);
        layout.addView(editBt);
        new android.app.AlertDialog.Builder(this)
                .setTitle("Настройки Arduino")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String ssid = editSsid.getText().toString().trim();
                    String bt = editBt.getText().toString().trim();
                    saveSettings(ssid, bt);
                    android.widget.Toast.makeText(this, "Настройки сохранены", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WIFI_ENABLE_REQUEST && resultCode == RESULT_OK) {
            scanAvailableNetworks();
        } else if (requestCode == BLUETOOTH_ENABLE_REQUEST && resultCode == RESULT_OK) {
            scanBluetoothDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleBluetoothAuthentication();
            } else {
                showToast("Разрешения Bluetooth не предоставлены");
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleWifiAuthentication();
            } else {
                showToast("Нужно разрешение геолокации");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (bluetoothReceiver != null) {
            try { unregisterReceiver(bluetoothReceiver); } catch (Exception ignored) {}
        }
        if (bluetoothSocket != null) {
            try { bluetoothSocket.close(); } catch (Exception ignored) {}
        }
    }
}
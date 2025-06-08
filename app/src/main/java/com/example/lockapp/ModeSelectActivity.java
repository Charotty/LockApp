package com.example.lockapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private static final String KEY_BT_NAME = "esp_bt_name";
    private String arduinoWifiSsid;
    private String arduinoBtName;

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> availableDevices = new ArrayList<>();
    private BroadcastReceiver bluetoothReceiver;
    private BluetoothSocket bluetoothSocket;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // --- WiFi параметры ESP ---
    // Для режима AP (точка доступа):
    private static final String ESP_AP_IP = "192.168.4.2";
    // Для режима STA (ESP в домашней сети):
    private static final String ESP_STA_IP = "192.168.1.42"; // <-- Замените на ваш статический IP, если нужен другой
    private static final int ESP_PORT = 8266; // Порт по умолчанию

    private String getCurrentEspIp() {
        // Можно добавить автоопределение по SSID, но для простоты — используем AP IP
        return ESP_AP_IP;
        // Если нужно выбрать другой режим — возвращайте ESP_STA_IP
    }

    // --- Bluetooth параметры ESP ---
    private String getCurrentBtName() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_BT_NAME, "ArduinoBT");
    }

    // --- WiFi Arduino connection state ---
    private String arduinoIp = null;
    private int arduinoPort = -1;

    private Handler statusHandler = new Handler(Looper.getMainLooper());
    private Runnable statusRunnable;
    private static final int STATUS_INTERVAL_MS = 4000;

    private ValueAnimator blinkAnimator;
    private boolean isBlinking = false;

    // Коды ошибок
    private static final int ERROR_NONE = 0;
    private static final int ERROR_WIFI_NOT_FOUND = 1;
    private static final int ERROR_BT_NOT_FOUND = 2;
    private static final int ERROR_WIFI_NOT_CONNECTED = 3;
    private static final int ERROR_BT_NOT_CONNECTED = 4;
    private static final int ERROR_UNKNOWN = 99;

    private long lastSuccessConnectionTime = 0;

    private static final int STORAGE_PERMISSION_REQUEST = 101;

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
        Button buttonCheckConnection = findViewById(R.id.buttonCheckConnection);
        Button detailsBtn = findViewById(R.id.buttonDiagnosticsDetails);

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
        setupLockButtons();
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        buttonCheckConnection.setOnClickListener(v -> onCheckConnectionClick(v));
        detailsBtn.setOnClickListener(v -> showDiagnosticsDetails());

        updateLockButtonsState();
        updateConnectionStatusUI();
        startStatusAutoUpdate();

        checkStoragePermission();
    }

    private boolean isConnectedToArduinoWifi() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID();
            // Убираем кавычки, если есть
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            // Для ESP AP часто бывает <unknown ssid>
            if ("<unknown ssid>".equals(ssid)) return true;
            return arduinoWifiSsid.equals(ssid);
        }
        return false;
    }

    private boolean isConnectedToArduinoBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
            if (paired != null) {
                String btName = getCurrentBtName();
                for (BluetoothDevice device : paired) {
                    if (btName.equals(device.getName())) {
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
        // 1. Показываем уведомление при возврате в приложение
        boolean wifiConnected = isConnectedToArduinoWifi();
        boolean btConnected = isConnectedToArduinoBluetooth();
        if (wifiConnected) {
            showToast("Подключено к замку по WiFi");
        }
        if (btConnected) {
            showToast("Подключено к замку по Bluetooth");
        }
        if (!wifiConnected && !btConnected) {
            showToast("Нет подключения к замку!");
        }
        updateLockButtonsState();
        updateConnectionStatusUI();
        startStatusAutoUpdate();
    }

    // 3. Делаем кнопки управления активными только при соединении
    private void updateLockButtonsState() {
        boolean connected = isConnectedToArduinoWifi() || isConnectedToArduinoBluetooth();
        Button wifiLockButton = findViewById(R.id.buttonLockWifi);
        Button btLockButton = findViewById(R.id.buttonLockBluetooth);
        // Проверяем на null (если кнопки есть в layout)
        if (wifiLockButton != null) wifiLockButton.setEnabled(connected);
        if (btLockButton != null) btLockButton.setEnabled(connected);
    }

    // 4. Добавляем явную кнопку проверки соединения
    public void onCheckConnectionClick(View view) {
        boolean wifiConnected = isConnectedToArduinoWifi();
        boolean btConnected = isConnectedToArduinoBluetooth();
        if (wifiConnected) {
            showToast("Подключено к замку по WiFi");
        } else if (btConnected) {
            showToast("Подключено к замку по Bluetooth");
        } else {
            showToast("Нет подключения к замку!");
        }
        updateLockButtonsState();
    }

    // ================= Wi-Fi Функционал =================
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN},
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

    private void sendWifiCommand(String cmd) {
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(getCurrentEspIp(), ESP_PORT), 3000);
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
        arduinoBtName = prefs.getString(KEY_BT_NAME, "ArduinoBT");
    }

    private void saveSettings(String ssid, String btName) {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_SSID, ssid).putString(KEY_BT_NAME, btName).apply();
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

    private void tryLockAction(Runnable action) {
        int errorCode = getConnectionErrorCode();
        if (errorCode == ERROR_NONE) {
            action.run();
        } else {
            showConnectionError(getConnectionErrorMessage(errorCode), errorCode, () -> updateConnectionStatusUI());
        }
    }

    private void setupLockButtons() {
        Button wifiLockButton = findViewById(R.id.buttonLockWifi);
        Button btLockButton = findViewById(R.id.buttonLockBluetooth);
        if (wifiLockButton != null) {
            wifiLockButton.setOnClickListener(v -> tryLockAction(() -> showLockControlDialogWifi()));
        }
        if (btLockButton != null) {
            btLockButton.setOnClickListener(v -> tryLockAction(() -> showLockControlDialogBluetooth()));
        }
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
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Доступ к памяти разрешён", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Приложению нужен доступ к памяти для работы с базой данных!", Toast.LENGTH_LONG).show();
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

    // 1. Автообновление статуса
    private void startStatusAutoUpdate() {
        if (statusRunnable == null) {
            statusRunnable = new Runnable() {
                @Override
                public void run() {
                    updateConnectionStatusUI();
                    statusHandler.postDelayed(this, STATUS_INTERVAL_MS);
                }
            };
        }
        statusHandler.post(statusRunnable);
    }
    private void stopStatusAutoUpdate() {
        statusHandler.removeCallbacksAndMessages(null);
    }

    // 2. Визуальная индикация статуса
    private void updateConnectionStatusUI() {
        View indicator = findViewById(R.id.connectionStatusIndicator);
        TextView statusText = findViewById(R.id.connectionStatusText);
        ImageView typeIcon = findViewById(R.id.connectionTypeIcon);
        boolean wifi = isConnectedToArduinoWifi();
        boolean bt = isConnectedToArduinoBluetooth();
        if (wifi) {
            indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_green));
            statusText.setText("Статус: подключено по WiFi");
            typeIcon.setImageResource(android.R.drawable.presence_online);
            stopBlinking(indicator);
            lastSuccessConnectionTime = System.currentTimeMillis();
        } else if (bt) {
            indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_green));
            statusText.setText("Статус: подключено по Bluetooth");
            typeIcon.setImageResource(android.R.drawable.presence_away);
            stopBlinking(indicator);
            lastSuccessConnectionTime = System.currentTimeMillis();
        } else {
            indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_red));
            statusText.setText("Статус: нет подключения");
            typeIcon.setImageResource(android.R.drawable.presence_offline);
            startBlinking(indicator);
        }
        updateDiagnosticsUI();
    }

    // 3. Диагностика соединения
    private void updateDiagnosticsUI() {
        TextView wifiInfo = findViewById(R.id.textWifiInfo);
        TextView btInfo = findViewById(R.id.textBtInfo);
        TextView lastSuccess = findViewById(R.id.textLastSuccess);
        // WiFi
        String wifiStr = "WiFi: ";
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null && info.getNetworkId() != -1) {
                wifiStr += "SSID: " + info.getSSID();
                wifiStr += ", RSSI: " + info.getRssi() + " дБм";
                wifiStr += ", IP: " + intToIp(info.getIpAddress());
            } else {
                wifiStr += "нет подключения";
            }
        } else {
            wifiStr += "WiFi выключен";
        }
        wifiInfo.setText(wifiStr);
        // Bluetooth
        String btStr = "Bluetooth: ";
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            boolean found = false;
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (getCurrentBtName().equals(device.getName())) {
                    btStr += "Имя: " + device.getName() + ", MAC: " + device.getAddress();
                    found = true;
                    break;
                }
            }
            if (!found) btStr += "устройство не найдено";
        } else {
            btStr += "Bluetooth выключен";
        }
        btInfo.setText(btStr);
        // Last success
        if (lastSuccessConnectionTime > 0) {
            String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date(lastSuccessConnectionTime));
            lastSuccess.setText("Последнее успешное соединение: " + time);
        } else {
            lastSuccess.setText("Последнее успешное соединение: -");
        }
    }
    private String intToIp(int ip) {
        return ((ip) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    private void showDiagnosticsDetails() {
        StringBuilder sb = new StringBuilder();
        // WiFi
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo info = null;
            try {
                info = wifiManager.getConnectionInfo();
            } catch (Exception e) {}
            if (info != null && info.getNetworkId() != -1) {
                sb.append("WiFi подключение:\n");
                sb.append("SSID: ").append(info.getSSID()).append("\n");
                sb.append("IP: ").append(intToIp(info.getIpAddress())).append("\n");
                sb.append("RSSI: ").append(info.getRssi()).append(" дБм\n");
                sb.append("MAC: ").append(info.getMacAddress() != null ? info.getMacAddress() : "нет данных").append("\n");
                sb.append("Link speed: ").append(info.getLinkSpeed()).append(" Mbps\n");
                sb.append("Supplicant state: ").append(info.getSupplicantState()).append("\n");
            } else {
                sb.append("WiFi: нет подключения\n");
            }
        } else {
            sb.append("WiFi выключен или не найден\n");
        }
        // Bluetooth
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> bonded = null;
            try {
                bonded = bluetoothAdapter.getBondedDevices();
            } catch (Exception e) {}
            boolean found = false;
            if (bonded != null) {
                for (BluetoothDevice device : bonded) {
                    if (getCurrentBtName().equals(device.getName())) {
                        sb.append("\nBluetooth подключение:\n");
                        sb.append("Имя: ").append(device.getName()).append("\n");
                        sb.append("MAC: ").append(device.getAddress()).append("\n");
                        found = true;
                        break;
                    }
                }
            }
            if (!found) sb.append("Bluetooth: устройство не найдено\n");
        } else {
            sb.append("Bluetooth выключен или не найден\n");
        }
        // Network info
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null) {
                    sb.append("\nТип сети: ").append(netInfo.getTypeName()).append("\n");
                    sb.append("Состояние: ").append(netInfo.getState()).append("\n");
                    sb.append("Детали: ").append(netInfo.getDetailedState()).append("\n");
                } else {
                    sb.append("\nНет активной сети\n");
                }
            } else {
                sb.append("\nНет данных о сети\n");
            }
        } catch (Exception e) {
            sb.append("\nОшибка получения сетевой информации\n");
        }
        // Last success
        if (lastSuccessConnectionTime > 0) {
            String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date(lastSuccessConnectionTime));
            sb.append("\nПоследнее успешное соединение: ").append(time).append("\n");
        }
        // Uptime
        sb.append("Аптайм системы: ").append(SystemClock.uptimeMillis() / 1000).append(" сек.\n");
        // Android version
        sb.append("Android version: ").append(Build.VERSION.RELEASE).append("\n");

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(sb.toString());
        textView.setPadding(24,24,24,24);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
            .setTitle("Расширенная диагностика соединения")
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show();
    }

    // 3. Диалог ошибки с кодом и кнопкой "Повторить попытку"
    private void showConnectionError(String reason, int errorCode, Runnable retryAction) {
        String codeMsg = "Код ошибки: " + errorCode;
        new AlertDialog.Builder(this)
            .setTitle("Ошибка соединения с замком")
            .setMessage(reason + "\n" + codeMsg +
                "\n\nВозможные действия:\n- Проверьте питание Arduino\n- Переподключитесь к WiFi/Bluetooth замка\n- Перезапустите Arduino\n- Проверьте правильность настроек SSID/имени Bluetooth")
            .setPositiveButton("Повторить попытку", (d, w) -> retryAction.run())
            .setNegativeButton("Отмена", null)
            .show();
    }

    private int getConnectionErrorCode() {
        if (!isWifiEnabled()) return ERROR_WIFI_NOT_FOUND;
        if (!isBluetoothEnabled()) return ERROR_BT_NOT_FOUND;
        if (!isConnectedToArduinoWifi() && !isConnectedToArduinoBluetooth()) return ERROR_WIFI_NOT_CONNECTED;
        return ERROR_NONE;
    }
    private String getConnectionErrorMessage(int code) {
        switch (code) {
            case ERROR_WIFI_NOT_FOUND: return "WiFi модуль не найден или выключен.";
            case ERROR_BT_NOT_FOUND: return "Bluetooth модуль не найден или выключен.";
            case ERROR_WIFI_NOT_CONNECTED: return "Нет подключения к замку.";
            default: return "Неизвестная ошибка.";
        }
    }
    private boolean isWifiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }
    private boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // Этап 2: Анимация мигания индикатора при отсутствии связи
    private void startBlinking(View indicator) {
        if (isBlinking) return;
        isBlinking = true;
        blinkAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), 0xFFF44336, 0x00FFFFFF, 0xFFF44336);
        blinkAnimator.setDuration(1200);
        blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blinkAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            indicator.setBackgroundColor(color);
        });
        blinkAnimator.start();
    }
    private void stopBlinking(View indicator) {
        if (blinkAnimator != null) {
            blinkAnimator.cancel();
            blinkAnimator = null;
        }
        isBlinking = false;
        indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_green));
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
        }
    }
}
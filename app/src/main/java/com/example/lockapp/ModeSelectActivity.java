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

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> availableDevices = new ArrayList<>();
    private BroadcastReceiver bluetoothReceiver;
    private BluetoothSocket bluetoothSocket;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String WIFI_MODULE_URL = "http://192.168.4.1"; // Arduino Wi-Fi module IP
    private final OkHttpClient simpleClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        Button buttonWifi = findViewById(R.id.buttonWifi);
        Button buttonBluetooth = findViewById(R.id.buttonBluetooth);
        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        Button buttonEventLog = findViewById(R.id.buttonEventLog);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        buttonWifi.setOnClickListener(v -> checkLocationPermission());
        buttonBluetooth.setOnClickListener(v -> checkBluetoothPermissions());
        buttonAdmin.setOnClickListener(v -> startActivity(new Intent(ModeSelectActivity.this, AdminTableActivity.class)));
        buttonEventLog.setOnClickListener(v -> startActivity(new Intent(ModeSelectActivity.this, EventLogActivity.class)));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WIFI_ENABLE_REQUEST && resultCode == RESULT_OK) {
            scanAvailableNetworks();
        } else if (requestCode == BLUETOOTH_ENABLE_REQUEST && resultCode == RESULT_OK) {
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

    // Диалог управления замком по Wi-Fi
    private void showLockControlDialogWifi() {
        String[] actions = {"Открыть замок", "Закрыть замок"};
        new AlertDialog.Builder(this)
                .setTitle("Управление замком (Wi-Fi)")
                .setItems(actions, (d, which) -> {
                    String cmd = which == 0 ? "/open" : "/close";
                    sendWifiCommand(cmd);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void sendWifiCommand(String path) {
        Request req = new Request.Builder()
                .url(WIFI_MODULE_URL + path)
                .get()
                .build();
        simpleClient.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Ошибка Wi-Fi: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> showToast(response.isSuccessful() ? "OK: " + response.code() : "Ошибка: " + response.code()));
                response.close();
            }
        });
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
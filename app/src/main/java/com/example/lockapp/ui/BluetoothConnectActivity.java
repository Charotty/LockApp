package com.example.lockapp.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lockapp.R;
import com.example.lockapp.api.BluetoothHelper;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothConnectActivity extends Activity {
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothHelper bluetoothHelper;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        statusText = findViewById(R.id.statusText);
        ListView listView = findViewById(R.id.deviceListView);
        Button refreshButton = findViewById(R.id.refreshButton);

        bluetoothHelper = new BluetoothHelper(this);
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(deviceListAdapter);

        refreshButton.setOnClickListener(v -> refreshDeviceList());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);
            statusText.setText("Подключение к " + device.getName() + "...");
            boolean connected = bluetoothHelper.connectToDevice(device);
            if (connected) {
                statusText.setText("Подключено к " + device.getName());
                Toast.makeText(this, "Успешно подключено!", Toast.LENGTH_SHORT).show();
            } else {
                statusText.setText("Ошибка подключения");
                Toast.makeText(this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
            }
        });

        checkBluetoothPermissions();
    }

    private void refreshDeviceList() {
        deviceListAdapter.clear();
        deviceList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothHelper.getPairedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                deviceList.add(device);
            }
        }
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                refreshDeviceList();
            }
        } else {
            refreshDeviceList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshDeviceList();
            } else {
                statusText.setText("Нет разрешения на Bluetooth");
            }
        }
    }
}

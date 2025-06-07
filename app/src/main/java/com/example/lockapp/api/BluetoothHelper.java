package com.example.lockapp.api;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public BluetoothHelper(Context context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        }
        return null;
    }

    public boolean connectToDevice(BluetoothDevice device) {
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Disconnect failed", e);
        }
    }

    public boolean sendData(String data) {
        try {
            if (outputStream != null) {
                outputStream.write(data.getBytes());
                outputStream.flush();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Send failed", e);
        }
        return false;
    }

    public String receiveData() {
        try {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int bytes = inputStream.read(buffer);
                return new String(buffer, 0, bytes);
            }
        } catch (IOException e) {
            Log.e(TAG, "Receive failed", e);
        }
        return null;
    }
}

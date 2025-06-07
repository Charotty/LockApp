package com.example.lockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class AdminTableActivity extends Activity {
    private int selectedUserPosition = -1;
    private List<User> users;
    private UserAdapter adapter;
    private UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_table);

        ImageButton buttonSave = findViewById(R.id.buttonSave);
        ImageButton buttonExit = findViewById(R.id.buttonExit);
        Button buttonAdd = findViewById(R.id.buttonAdd);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        Button buttonResetUser = findViewById(R.id.buttonResetUser);
        Button buttonSyncFromArduino = findViewById(R.id.buttonSyncFromArduino);
        Button buttonSyncToArduino = findViewById(R.id.buttonSyncToArduino);
        ListView userTable = findViewById(R.id.userTable);

        dbHelper = new UserDatabaseHelper(this);
        users = dbHelper.getAllUsers();
        adapter = new UserAdapter(this, users);
        userTable.setAdapter(adapter);

        userTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUserPosition = position;
                Toast.makeText(AdminTableActivity.this, "Выбран пользователь: " + users.get(position).fio, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Кнопка ДОБАВИТЬ ---
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPopup();
            }
        });

        // --- Кнопка УДАЛИТЬ ---
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                    Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                    return;
                }
                User user = users.get(selectedUserPosition);
                dbHelper.deleteUser(user.id);
                users.remove(selectedUserPosition);
                adapter.notifyDataSetChanged();
                selectedUserPosition = -1;
            }
        });

        // --- Кнопка СБРОС ПОЛЬЗОВАТЕЛЯ (очищает только RFID и отпечаток) ---
        buttonResetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
                    Toast.makeText(AdminTableActivity.this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
                    return;
                }
                User user = users.get(selectedUserPosition);
                new AlertDialog.Builder(AdminTableActivity.this)
                        .setTitle("Сбросить RFID и отпечаток?")
                        .setMessage("ФИО: " + user.fio)
                        .setPositiveButton("Сбросить", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                user.rfid = "";
                                user.fingerprint = "";
                                dbHelper.updateUser(user);
                                users.set(selectedUserPosition, user);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(AdminTableActivity.this, "Сброс выполнен", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminTableActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonSyncFromArduino.setOnClickListener(v -> {
            if (!isConnectedToArduino()) {
                Toast.makeText(this, "Нет подключения к Arduino по WiFi или Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            syncUsersFromArduino();
        });
        buttonSyncToArduino.setOnClickListener(v -> {
            if (!isConnectedToArduino()) {
                Toast.makeText(this, "Нет подключения к Arduino по WiFi или Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            syncUsersToArduino();
        });
    }

    // --- Popup окно для добавления ---
    private void showAddPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить пользователя");
        View view = getLayoutInflater().inflate(R.layout.dialog_user, null);
        EditText editFio = view.findViewById(R.id.editFio);
        EditText editPosition = view.findViewById(R.id.editPosition);
        EditText editPassword = view.findViewById(R.id.editPassword);
        // ... другие поля при необходимости

        builder.setView(view);
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String fio = editFio.getText().toString().trim();
            String position = editPosition.getText().toString().trim();
            String password = editPassword.getText().toString();
            if (fio.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните ФИО и пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            // Проверка на уникальность ФИО
            for (User u : users) {
                if (fio.equals(u.fio)) {
                    Toast.makeText(this, "Пользователь с таким ФИО уже есть", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            User user = new User(0, fio, position, password, "", "", "", "");
            dbHelper.insertUser(user);
            // Получить id из базы, если нужно
            List<User> all = dbHelper.getAllUsers();
            for (User u : all) {
                if (fio.equals(u.fio)) {
                    user.id = u.id;
                    break;
                }
            }
            users.add(user);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // --- Проверка подключения к Arduino по WiFi или Bluetooth ---
    private boolean isConnectedToArduino() {
        return isConnectedToArduinoWifi() || isConnectedToArduinoBluetooth();
    }
    private boolean isConnectedToArduinoWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) return false;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return "ArduinoLock".equals(ssid); // замените на ваш SSID
        }
        return false;
    }
    private boolean isConnectedToArduinoBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if ("ArduinoBT".equals(device.getName())) { // замените на ваше имя устройства
                    return true;
                }
            }
        }
        return false;
    }

    // Получение пользователей с Arduino (только id/rfid/fingerprint)
    private void syncUsersFromArduino() {
        // TODO: Реализовать обмен по Bluetooth/WiFi, примерная заглушка:
        // 1. Отправить команду "GET_USERS" на Arduino
        // 2. Получить список пользователей (id, rfid, fingerprint)
        // 3. Обновить локальную таблицу пользователей (fio не трогать)
        // 4. Сохранить полученный список в event log
        Toast.makeText(this, "Получение списка пользователей с Arduino...", Toast.LENGTH_SHORT).show();
        // ...
    }

    // Отправка пользователей на Arduino (только id/rfid/fingerprint)
    private void syncUsersToArduino() {
        // TODO: Реализовать обмен по Bluetooth/WiFi, примерная заглушка:
        // 1. Сформировать список пользователей (id, rfid, fingerprint)
        // 2. Отправить команду "SET_USERS" с этим списком на Arduino
        Toast.makeText(this, "Отправка списка пользователей на Arduino...", Toast.LENGTH_SHORT).show();
        // ...
    }
}

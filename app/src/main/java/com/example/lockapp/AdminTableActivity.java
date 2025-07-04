package com.example.lockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import com.example.lockapp.crypto.KuznechikUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AdminTableActivity extends Activity {
    private int selectedUserPosition = -1;
    private List<User> users;
    private UserAdapter adapter;
    private UserDatabaseHelper dbHelper;

    // Ключ и IV должны совпадать с Arduino (пример, замените на свои!)
    private static final byte[] KUZ_KEY = new byte[32]; // TODO: заполнить своим ключом
    private static final byte[] KUZ_IV = new byte[16];  // TODO: заполнить своим IV

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
            if (!isConnectedToArduinoWifi()) {
                Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
                return;
            }
            syncUsersFromArduino();
        });
        buttonSyncToArduino.setOnClickListener(v -> {
            if (!isConnectedToArduinoWifi()) {
                Toast.makeText(this, "Нет подключения к Arduino по WiFi", Toast.LENGTH_SHORT).show();
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

    // --- Проверка подключения к Arduino по WiFi ---
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

    // Отправка пользователей на Arduino (WiFi, BLOB, шифрование)
    private void syncUsersToArduino() {
        try {
            String json = new Gson().toJson(users);
            byte[] plain = json.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = KuznechikUtil.encrypt(plain, KUZ_KEY, KUZ_IV);
            // TODO: отправить encrypted по WiFi (BLOB)
            // Например: wifiOutputStream.write(encrypted);
            Toast.makeText(this, "Данные отправлены (зашифровано)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка шифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Получение пользователей с Arduino (WiFi, BLOB, дешифрование)
    private void syncUsersFromArduino() {
        try {
            // TODO: получить encrypted с Arduino (BLOB)
            // Например: byte[] encrypted = wifiInputStream.read(...);
            byte[] encrypted = new byte[0]; // заглушка
            byte[] plain = KuznechikUtil.decrypt(encrypted, KUZ_KEY, KUZ_IV);
            String json = new String(plain, StandardCharsets.UTF_8);
            List<User> arduinoUsers = new Gson().fromJson(json, new TypeToken<List<User>>(){}.getType());
            // Обновить локальную базу/список
            // users.clear(); users.addAll(arduinoUsers); adapter.notifyDataSetChanged();
            Toast.makeText(this, "Данные получены (расшифровано)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка дешифрования: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

package com.example.lockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lockapp.UserDatabaseHelper;
import com.example.lockapp.User;
import java.util.List;
import java.util.HashMap;

public class LoginActivity extends Activity {
    private HashMap<String, Integer> attemptsMap = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        EditText editFio = findViewById(R.id.editTextUsername);
        EditText editPassword = findViewById(R.id.editTextPassword);
        // Button buttonWifi = findViewById(R.id.buttonWifiLogin); // удалено, кнопки больше нет

        UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);

        // Добавление тестового пользователя "Админ Тестовый" если его нет
        boolean hasAdmin = false;
        for (User u : dbHelper.getAllUsers()) {
            if ("Админ Тестовый".equals(u.fio)) {
                hasAdmin = true;
                break;
            }
        }
        if (!hasAdmin) {
            User admin = new User(0, "Админ Тестовый", "Администратор", "admin123", "", "", "", "");
            dbHelper.insertUser(admin);
        }

        buttonLogin.setOnClickListener(v -> {
            String fio = editFio.getText().toString().trim();
            String password = editPassword.getText().toString();
            if (fio.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Введите ФИО и пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            // Поиск пользователя по ФИО и паролю
            List<User> users = dbHelper.getAllUsers();
            boolean found = false;
            for (User user : users) {
                if (fio.equals(user.fio) && password.equals(user.password)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, ModeSelectActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Неверные ФИО или пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

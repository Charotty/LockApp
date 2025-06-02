package com.example.lockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lockapp.api.ApiClient;
import com.example.lockapp.api.UserApi;
import com.example.lockapp.api.EventLogApi;
import com.example.lockapp.EventLog;
import com.example.lockapp.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        EventLogApi eventLogApi = ApiClient.getClient().create(EventLogApi.class);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fio = editFio.getText().toString().trim();
                String password = editPassword.getText().toString();
                if (fio.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Введите ФИО и пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                userApi.getUsers().enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> userList = response.body();
                            User foundUser = null;
                            for (User u : userList) {
                                if (u.fio.equals(fio)) {
                                    foundUser = u;
                                    break;
                                }
                            }
                            if (foundUser != null) {
                                String hash = md5(password);
                                int attempts = attemptsMap.getOrDefault(fio, 0);
                                if (foundUser.password.equals(hash)) {
                                    // Успешный вход
                                    attemptsMap.put(fio, 0);
                                    EventLog event = new EventLog(fio, foundUser.position, "Пароль", "Открыт замок", 1, null);
                                    eventLogApi.addEvent(event).enqueue(new Callback<EventLog>() {
                                        @Override
                                        public void onResponse(Call<EventLog> call, Response<EventLog> response) {}
                                        @Override
                                        public void onFailure(Call<EventLog> call, Throwable t) {}
                                    });
                                    Intent intent = new Intent(LoginActivity.this, ModeSelectActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    attempts++;
                                    attemptsMap.put(fio, attempts);
                                    EventLog event = new EventLog(fio, foundUser.position, "Пароль", "Неудачная попытка входа по паролю", attempts, null);
                                    eventLogApi.addEvent(event).enqueue(new Callback<EventLog>() {
                                        @Override
                                        public void onResponse(Call<EventLog> call, Response<EventLog> response) {}
                                        @Override
                                        public void onFailure(Call<EventLog> call, Throwable t) {}
                                    });
                                    if (attempts >= MAX_ATTEMPTS) {
                                        EventLog event2 = new EventLog(fio, foundUser.position, "Пароль", "Доступ заблокирован", attempts, null);
                                        eventLogApi.addEvent(event2).enqueue(new Callback<EventLog>() {
                                            @Override
                                            public void onResponse(Call<EventLog> call, Response<EventLog> response) {}
                                            @Override
                                            public void onFailure(Call<EventLog> call, Throwable t) {}
                                        });
                                        Toast.makeText(LoginActivity.this, "Доступ заблокирован!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Неверный пароль! Попытка " + attempts, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Ошибка связи с сервером", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Ошибка связи с сервером", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}

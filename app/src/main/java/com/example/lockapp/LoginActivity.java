package com.example.lockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lockapp.api.ApiClient;
import com.example.lockapp.api.UserApi;
import com.example.lockapp.api.EventLogApi;
import com.example.lockapp.api.AuthApi;
import com.example.lockapp.api.model.LoginRequest;
import com.example.lockapp.api.model.AuthResponse;
import com.example.lockapp.EventLog;
import com.example.lockapp.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Request;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;
import java.security.GeneralSecurityException;
import java.io.IOException;

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

        // Инициализация клиента с логированием
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(chain -> {
                Request request = chain.request();
                String token = getSecurePrefs().getString("auth_token", null);
                if (token != null) {
                    request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                }
                return chain.proceed(request);
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        // Инициализируем EncryptedSharedPreferences
        SharedPreferences securePrefs = getSecurePrefs();

        // Обновляем ApiClient, чтобы использовать кастомный OkHttpClient
        ApiClient.initClient(client);

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
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
                // Отправка запроса на аутентификацию
                Log.d("LOGIN_TEST", "Введено ФИО: " + fio + ", пароль: " + password);
                authApi.login(new LoginRequest(fio, password)).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            handleLogin(response.body().getToken());
                        } else if (response.code() == 401) {
                            Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 423) {
                            Toast.makeText(LoginActivity.this, "Доступ заблокирован", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void handleLogin(String token) {
        // Сохраняем токен в securePrefs
        SharedPreferences securePrefs = getSecurePrefs();
        securePrefs.edit().putString("auth_token", token).apply();
        Intent intent = new Intent(LoginActivity.this, ModeSelectActivity.class);
        startActivity(intent);
        finish();
    }

    private SharedPreferences getSecurePrefs() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
            return EncryptedSharedPreferences.create(
                this,
                "auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to get secure prefs", e);
        }
    }
}

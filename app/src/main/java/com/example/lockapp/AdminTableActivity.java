package com.example.lockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.lockapp.api.ApiClient;
import com.example.lockapp.api.UserApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AdminTableActivity extends Activity {
    private int selectedUserPosition = -1;
    private List<User> users;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_table);

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonExit = findViewById(R.id.buttonExit);
        Button buttonAdd = findViewById(R.id.buttonAdd);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        Button buttonResetUser = findViewById(R.id.buttonResetUser);
        ListView userTable = findViewById(R.id.userTable);

        users = new ArrayList<>();
        adapter = new UserAdapter(this, users);
        userTable.setAdapter(adapter);

        userTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUserPosition = position;
                Toast.makeText(AdminTableActivity.this, "Выбран пользователь: " + users.get(position).fio, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Retrofit: загрузка пользователей с сервера ---
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminTableActivity.this, "Ошибка загрузки пользователей с сервера", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Кнопка ДОБАВИТЬ ---
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Пример добавления пользователя через API (можно доработать под ваш диалог)
                User newUser = new User();
                newUser.fio = "Новый пользователь";
                newUser.password = Integer.toHexString("1234".hashCode());
                userApi.addUser(newUser).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            users.add(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(AdminTableActivity.this, "Ошибка добавления пользователя", Toast.LENGTH_SHORT).show();
                    }
                });
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
                userApi.deleteUser(user.id).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        users.remove(selectedUserPosition);
                        adapter.notifyDataSetChanged();
                        selectedUserPosition = -1;
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AdminTableActivity.this, "Ошибка удаления пользователя", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                // TODO: реализовать сброс RFID и отпечатка через API
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
    }

    // --- Popup окно для добавления ---
    private void showAddPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить");
        String[] actions = {"Новый пользователь", "RFID", "Отпечаток"};
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Новый пользователь: показать форму, имитировать ожидание ответа Arduino
                        showAddUserWithLoading();
                        break;
                    case 1:
                        // Добавить RFID существующему пользователю
                        addRfidToUser();
                        break;
                    case 2:
                        // Добавить отпечаток существующему пользователю
                        addFingerprintToUser();
                        break;
                }
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // --- Заглушка: добавить нового пользователя с ожиданием ---
    private void showAddUserWithLoading() {
        // Здесь показываем ProgressDialog, имитируем обмен с Arduino, затем показываем форму добавления
        Toast.makeText(this, "Ожидание ответа от Arduino (заглушка)...", Toast.LENGTH_SHORT).show();
        // TODO: реализовать реальный обмен с Arduino и форму
    }

    // --- Заглушка: добавить RFID пользователю ---
    private void addRfidToUser() {
        if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
            Toast.makeText(this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Здесь показываем ProgressDialog, имитируем обмен с Arduino, затем обновляем поле RFID
        Toast.makeText(this, "Добавление RFID (заглушка)...", Toast.LENGTH_SHORT).show();
        // TODO: реализовать реальный обмен с Arduino, обновление поля и отправку на сервер
    }

    // --- Заглушка: добавить отпечаток пользователю ---
    private void addFingerprintToUser() {
        if (selectedUserPosition < 0 || selectedUserPosition >= users.size()) {
            Toast.makeText(this, "Сначала выберите пользователя!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Здесь показываем ProgressDialog, имитируем обмен с Arduino, затем обновляем поле отпечаток
        Toast.makeText(this, "Добавление отпечатка (заглушка)...", Toast.LENGTH_SHORT).show();
        // TODO: реализовать реальный обмен с Arduino, обновление поля и отправку на сервер
    }
}

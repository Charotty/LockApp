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
import android.widget.LinearLayout;
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
                } else {
                    String errorMsg = "Ошибка: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ", " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ", errorBody parse error: " + e.getMessage();
                    }
                    Toast.makeText(AdminTableActivity.this, "Ошибка загрузки пользователей: " + errorMsg, Toast.LENGTH_LONG).show();
                    android.util.Log.e("API_ERROR", errorMsg);
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminTableActivity.this, "Ошибка загрузки пользователей: " + t.getMessage(), Toast.LENGTH_LONG).show();
                android.util.Log.e("API_ERROR", "onFailure: ", t);
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
                                // Вызов API сброса
                                UserApi api = ApiClient.getClient().create(UserApi.class);
                                api.resetUser(user.id).enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {
                                        if (response.isSuccessful()) {
                                            users.set(selectedUserPosition, response.body());
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(AdminTableActivity.this, "Сброс выполнен", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AdminTableActivity.this, "Ошибка сброса: " + response.code(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {
                                        Toast.makeText(AdminTableActivity.this, "Сетевая ошибка при сбросе", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

    // --- Форма добавления нового пользователя ---
    private void showAddUserWithLoading() {
        // Показать форму ввода данных нового пользователя
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить нового пользователя");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText inputFio = new EditText(this);
        inputFio.setHint("ФИО"); layout.addView(inputFio);
        final EditText inputPosition = new EditText(this);
        inputPosition.setHint("Должность"); layout.addView(inputPosition);
        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Пароль");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);
        builder.setView(layout);
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String fio = inputFio.getText().toString().trim();
            String pos = inputPosition.getText().toString().trim();
            String pwd = inputPassword.getText().toString();
            if (fio.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "ФИО и пароль обязательны", Toast.LENGTH_SHORT).show();
                return;
            }
            UserApi api = ApiClient.getClient().create(UserApi.class);
            User newUser = new User();
            newUser.fio = fio;
            newUser.position = pos;
            newUser.password = Integer.toHexString(pwd.hashCode());
            api.addUser(newUser).enqueue(new Callback<User>() {
                @Override public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        users.add(response.body()); adapter.notifyDataSetChanged();
                        Toast.makeText(AdminTableActivity.this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(AdminTableActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // --- Добавить RFID пользователю ---
    private void addRfidToUser() {
        // Запрос ввода нового RFID
        User user = users.get(selectedUserPosition);
        AlertDialog.Builder builderR = new AlertDialog.Builder(this);
        builderR.setTitle("Введите RFID");
        final EditText inputR = new EditText(this);
        inputR.setHint("RFID (hex)");
        builderR.setView(inputR);
        builderR.setPositiveButton("ОК", (dialog, which) -> {
            String rfid = inputR.getText().toString().trim();
            if (rfid.isEmpty()) return;
            UserApi api = ApiClient.getClient().create(UserApi.class);
            User upd = new User(); upd.id = user.id; upd.rfid = rfid;
            api.updateUser(user.id, upd).enqueue(new Callback<User>() {
                @Override public void onResponse(Call<User> call, Response<User> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        users.set(selectedUserPosition, r.body()); adapter.notifyDataSetChanged();
                        Toast.makeText(AdminTableActivity.this, "RFID обновлен", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(AdminTableActivity.this, "Ошибка обновления RFID", Toast.LENGTH_SHORT).show();
                }
            });
        }); builderR.setNegativeButton("Отмена", null); builderR.show();
    }

    // --- Добавить отпечаток пользователю ---
    private void addFingerprintToUser() {
        // Запрос ввода нового отпечатка
        User user = users.get(selectedUserPosition);
        AlertDialog.Builder builderF = new AlertDialog.Builder(this);
        builderF.setTitle("Введите отпечаток");
        final EditText inputF = new EditText(this);
        inputF.setHint("Fingerprint (base64)");
        builderF.setView(inputF);
        builderF.setPositiveButton("ОК", (dialog, which) -> {
            String fp = inputF.getText().toString().trim();
            if (fp.isEmpty()) return;
            UserApi api = ApiClient.getClient().create(UserApi.class);
            User upd = new User(); upd.id = user.id; upd.fingerprint = fp;
            api.updateUser(user.id, upd).enqueue(new Callback<User>() {
                @Override public void onResponse(Call<User> call, Response<User> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        users.set(selectedUserPosition, r.body()); adapter.notifyDataSetChanged();
                        Toast.makeText(AdminTableActivity.this, "Отпечаток обновлен", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(AdminTableActivity.this, "Ошибка обновления отпечатка", Toast.LENGTH_SHORT).show();
                }
            });
        }); builderF.setNegativeButton("Отмена", null); builderF.show();
    }
}

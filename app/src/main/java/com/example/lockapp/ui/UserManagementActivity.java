package com.example.lockapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.lockapp.R;
import com.example.lockapp.User;
import com.example.lockapp.UserAdapter;
import com.example.lockapp.UserDatabaseHelper;
import com.example.lockapp.api.BluetoothHelper;
import com.example.lockapp.api.UserParser;
import java.util.List;

public class UserManagementActivity extends Activity {
    private UserDatabaseHelper dbHelper;
    private BluetoothHelper bluetoothHelper;
    private List<User> users;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        dbHelper = new UserDatabaseHelper(this);
        bluetoothHelper = new BluetoothHelper(this);
        ListView listView = findViewById(R.id.userListView);
        Button addButton = findViewById(R.id.addUserButton);
        Button exportButton = findViewById(R.id.exportUsersButton);
        users = dbHelper.getAllUsers();
        adapter = new UserAdapter(this, users);
        listView.setAdapter(adapter);
        refreshUsers();

        addButton.setOnClickListener(v -> showUserDialog(null));
        exportButton.setOnClickListener(v -> exportUsers());

        listView.setOnItemClickListener((parent, view, position, id) -> showUserDialog(users.get(position)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            User user = users.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Удалить пользователя?")
                    .setMessage(user.fio)
                    .setPositiveButton("Удалить", (d, w) -> {
                        dbHelper.deleteUser(user.id);
                        refreshUsers();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    private void refreshUsers() {
        users.clear();
        users.addAll(dbHelper.getAllUsers());
        adapter.notifyDataSetChanged();
    }

    private void showUserDialog(User user) {
        boolean isEdit = user != null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user, null);
        EditText editFio = dialogView.findViewById(R.id.editFio);
        EditText editPosition = dialogView.findViewById(R.id.editPosition);
        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        EditText editRfid = dialogView.findViewById(R.id.editRfid);
        EditText editFingerprint = dialogView.findViewById(R.id.editFingerprint);
        EditText editIvRfid = dialogView.findViewById(R.id.editIvRfid);
        EditText editIvFingerprint = dialogView.findViewById(R.id.editIvFingerprint);
        if (isEdit) {
            editFio.setText(user.fio);
            editPosition.setText(user.position);
            editPassword.setText(user.password);
            editRfid.setText(user.rfidStr());
            editFingerprint.setText(user.fingerprintStr());
            editIvRfid.setText(user.ivRfidStr());
            editIvFingerprint.setText(user.ivFingerprintStr());
        }
        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Редактировать пользователя" : "Добавить пользователя")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (d, w) -> {
                    User u = isEdit ? user : new User();
                    u.fio = editFio.getText().toString();
                    u.position = editPosition.getText().toString();
                    u.password = editPassword.getText().toString();
                    u.rfid = editRfid.getText().toString();
                    u.fingerprint = editFingerprint.getText().toString();
                    u.ivRfid = editIvRfid.getText().toString();
                    u.ivFingerprint = editIvFingerprint.getText().toString();
                    if (isEdit) {
                        dbHelper.updateUser(u);
                    } else {
                        dbHelper.insertUser(u);
                    }
                    refreshUsers();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void exportUsers() {
        List<User> allUsers = dbHelper.getAllUsers();
        String usersCsv = UserParser.toCSV(allUsers);
        boolean sent = bluetoothHelper.sendData(usersCsv);
        if (sent) {
            Toast.makeText(this, "Пользователи отправлены на Arduino", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка отправки через Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.lockapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsActivity extends Activity {
    private static final int REQ_EXPORT_USERS = 1;
    private static final int REQ_IMPORT_USERS = 2;
    private static final int REQ_EXPORT_LOG = 3;
    private static final int REQ_IMPORT_LOG = 4;

    private static final String PREFS = "lockapp_prefs";
    private static final String KEY_BT_NAME = "esp_bt_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button exportUsers = findViewById(R.id.buttonExportUsersDb);
        Button importUsers = findViewById(R.id.buttonImportUsersDb);
        Button exportLog = findViewById(R.id.buttonExportLogDb);
        Button importLog = findViewById(R.id.buttonImportLogDb);

        exportUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { exportDb("users.db", REQ_EXPORT_USERS); }
        });
        importUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { importDb("users.db", REQ_IMPORT_USERS); }
        });
        exportLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { exportDb("eventlog.db", REQ_EXPORT_LOG); }
        });
        importLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { importDb("eventlog.db", REQ_IMPORT_LOG); }
        });

        EditText editBtName = findViewById(R.id.editTextBtName);
        Button btnSaveBtName = findViewById(R.id.buttonSaveBtName);
        // Подгружаем текущее значение
        String btName = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_BT_NAME, "ArduinoBT");
        editBtName.setText(btName);
        btnSaveBtName.setOnClickListener(v -> {
            String name = editBtName.getText().toString().trim();
            if (!name.isEmpty()) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BT_NAME, name).apply();
                Toast.makeText(this, "Имя Bluetooth сохранено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Введите имя Bluetooth", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportDb(String dbName, int reqCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, dbName);
        startActivityForResult(intent, reqCode);
    }

    private void importDb(String dbName, int reqCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (requestCode == REQ_EXPORT_USERS) {
                copyDbToUri("users.db", uri);
                Toast.makeText(SettingsActivity.this, "users.db экспортирована", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQ_IMPORT_USERS) {
                copyUriToDb(uri, "users.db");
                Toast.makeText(SettingsActivity.this, "users.db импортирована", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQ_EXPORT_LOG) {
                copyDbToUri("eventlog.db", uri);
                Toast.makeText(SettingsActivity.this, "eventlog.db экспортирована", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQ_IMPORT_LOG) {
                copyUriToDb(uri, "eventlog.db");
                Toast.makeText(SettingsActivity.this, "eventlog.db импортирована", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(SettingsActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyDbToUri(String dbName, Uri uri) throws Exception {
        File dbFile = getDatabasePath(dbName);
        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = getContentResolver().openOutputStream(uri)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }

    private void copyUriToDb(Uri uri, String dbName) throws Exception {
        File dbFile = getDatabasePath(dbName);
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dbFile, false)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }
}

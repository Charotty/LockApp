package com.example.lockapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventLogActivity extends Activity {
    private EventLogDatabaseHelper dbHelper;
    private List<EventLog> events;
    private EventLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_log);
        ListView listView = findViewById(R.id.eventLogList);
        dbHelper = new EventLogDatabaseHelper(this);
        events = new ArrayList<>();
        adapter = new EventLogAdapter(this, events);
        listView.setAdapter(adapter);
        loadEvents(null, null, null);
        // Фильтрация по кнопке
        Button filterButton = findViewById(R.id.filterButton);
        EditText dateEdit = findViewById(R.id.filterDate);
        EditText userEdit = findViewById(R.id.filterUser);
        EditText statusEdit = findViewById(R.id.filterStatus);
        filterButton.setOnClickListener(v -> {
            String date = dateEdit.getText().toString();
            String user = userEdit.getText().toString();
            String status = statusEdit.getText().toString();
            loadEvents(date, user, status);
        });
        // Кнопка "Назад" (стрелка)
        ImageButton backBtn = findViewById(R.id.buttonBack);
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadEvents(String date, String user, String status) {
        events.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = "1=1";
        List<String> args = new ArrayList<>();
        if (date != null && !date.isEmpty()) {
            selection += " AND date(" + EventLogDatabaseHelper.COLUMN_TIMESTAMP + ") = ?";
            args.add(date);
        }
        if (user != null && !user.isEmpty()) {
            selection += " AND " + EventLogDatabaseHelper.COLUMN_FIO + " LIKE ?";
            args.add("%" + user + "%");
        }
        if (status != null && !status.isEmpty()) {
            selection += " AND " + EventLogDatabaseHelper.COLUMN_RESULT + " LIKE ?";
            args.add("%" + status + "%");
        }
        Cursor cursor = db.query(EventLogDatabaseHelper.TABLE_EVENT_LOG, null, selection, args.toArray(new String[0]), null, null, EventLogDatabaseHelper.COLUMN_TIMESTAMP + " DESC");
        while (cursor.moveToNext()) {
            String fio = cursor.getString(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_FIO));
            String position = cursor.getString(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_POSITION));
            String method = cursor.getString(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_METHOD));
            String result = cursor.getString(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_RESULT));
            int attempt = cursor.getInt(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_ATTEMPT));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(EventLogDatabaseHelper.COLUMN_TIMESTAMP));
            events.add(new EventLog(fio, position, method, result, attempt, timestamp));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}

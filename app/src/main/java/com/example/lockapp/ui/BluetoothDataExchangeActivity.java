package com.example.lockapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lockapp.R;
import com.example.lockapp.api.BluetoothHelper;
import com.example.lockapp.EventLog;
import com.example.lockapp.EventLogDatabaseHelper;
import com.example.lockapp.api.EventLogParser;
import java.util.List;

public class BluetoothDataExchangeActivity extends Activity {
    private BluetoothHelper bluetoothHelper;
    private EventLogDatabaseHelper dbHelper;
    private TextView receivedDataText;
    private EditText sendCommandEdit;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_data_exchange);

        bluetoothHelper = new BluetoothHelper(this);
        dbHelper = new EventLogDatabaseHelper(this);
        receivedDataText = findViewById(R.id.receivedDataText);
        sendCommandEdit = findViewById(R.id.sendCommandEdit);
        Button sendButton = findViewById(R.id.sendButton);
        Button receiveButton = findViewById(R.id.receiveButton);
        scrollView = findViewById(R.id.scrollView);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = sendCommandEdit.getText().toString();
                if (!command.isEmpty()) {
                    boolean sent = bluetoothHelper.sendData(command + "\n");
                    if (sent) {
                        Toast.makeText(BluetoothDataExchangeActivity.this, "Команда отправлена", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BluetoothDataExchangeActivity.this, "Ошибка отправки", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = bluetoothHelper.receiveData();
                if (data != null) {
                    receivedDataText.append(data + "\n");
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    // Попробуем распарсить и сохранить как логи
                    List<EventLog> logs;
                    if (data.trim().startsWith("[") || data.trim().startsWith("{")) {
                        // JSON
                        try {
                            logs = EventLogParser.parseJSON(data);
                        } catch (Exception e) {
                            Toast.makeText(BluetoothDataExchangeActivity.this, "Ошибка парсинга JSON", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        // CSV
                        try {
                            logs = EventLogParser.parseCSV(data);
                        } catch (Exception e) {
                            Toast.makeText(BluetoothDataExchangeActivity.this, "Ошибка парсинга CSV", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    for (EventLog log : logs) {
                        dbHelper.insertEvent(log);
                    }
                    Toast.makeText(BluetoothDataExchangeActivity.this, "Логи сохранены в журнал", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BluetoothDataExchangeActivity.this, "Нет данных", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

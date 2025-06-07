package com.example.lockapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lockapp.R;

public class WiFiConnectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);

        EditText editIp = findViewById(R.id.editTextIp);
        EditText editPort = findViewById(R.id.editTextPort);
        Button buttonConnect = findViewById(R.id.buttonConnectWifi);

        buttonConnect.setOnClickListener(v -> {
            String ip = editIp.getText().toString().trim();
            String portStr = editPort.getText().toString().trim();
            if (ip.isEmpty() || portStr.isEmpty()) {
                Toast.makeText(this, "Введите IP и порт Arduino", Toast.LENGTH_SHORT).show();
                return;
            }
            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректный порт", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(WiFiConnectActivity.this, WiFiDataExchangeActivity.class);
            intent.putExtra("ip", ip);
            intent.putExtra("port", port);
            startActivity(intent);
        });
    }
}

package com.example.lockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ModeSelectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        Button buttonWifi = findViewById(R.id.buttonWifi);
        Button buttonBluetooth = findViewById(R.id.buttonBluetooth);
        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        Button buttonEventLog = findViewById(R.id.buttonEventLog);

        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Реализуйте вход по Wi-Fi
            }
        });
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Реализуйте вход по Bluetooth
            }
        });
        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeSelectActivity.this, AdminTableActivity.class);
                startActivity(intent);
            }
        });
        buttonEventLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeSelectActivity.this, EventLogActivity.class);
                startActivity(intent);
            }
        });
    }
}

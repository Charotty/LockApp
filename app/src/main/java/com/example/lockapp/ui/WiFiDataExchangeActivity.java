package com.example.lockapp.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lockapp.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WiFiDataExchangeActivity extends Activity {
    private EditText editCommand;
    private TextView textResponse;
    private String ip;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    private BufferedReader inputReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_data_exchange);

        editCommand = findViewById(R.id.editTextCommand);
        textResponse = findViewById(R.id.textViewResponse);
        Button buttonSend = findViewById(R.id.buttonSendCommand);

        ip = getIntent().getStringExtra("ip");
        port = getIntent().getIntExtra("port", 0);

        new ConnectTask().execute();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = editCommand.getText().toString().trim();
                if (cmd.isEmpty()) {
                    Toast.makeText(WiFiDataExchangeActivity.this, "Введите команду", Toast.LENGTH_SHORT).show();
                    return;
                }
                new SendCommandTask().execute(cmd);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 3000);
                outputStream = socket.getOutputStream();
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean connected) {
            if (!connected) {
                Toast.makeText(WiFiDataExchangeActivity.this, "Ошибка подключения к Arduino", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(WiFiDataExchangeActivity.this, "Подключено к Arduino", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendCommandTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String cmd = params[0] + "\n";
                outputStream.write(cmd.getBytes());
                outputStream.flush();
                return inputReader.readLine();
            } catch (IOException e) {
                return "Ошибка обмена: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String response) {
            textResponse.setText(response);
        }
    }
}

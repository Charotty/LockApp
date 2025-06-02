package com.example.lockapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import com.example.lockapp.api.ApiClient;
import com.example.lockapp.api.EventLogApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class EventLogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_log);
        ListView listView = findViewById(R.id.eventLogList);
        List<EventLog> events = new ArrayList<>();
        // --- Загрузка событий с сервера через Retrofit ---
        EventLogApi eventLogApi = ApiClient.getClient().create(EventLogApi.class);
        eventLogApi.getEvents().enqueue(new Callback<List<EventLog>>() {
            @Override
            public void onResponse(Call<List<EventLog>> call, Response<List<EventLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    events.clear();
                    events.addAll(response.body());
                    EventLogAdapter adapter = new EventLogAdapter(EventLogActivity.this, events);
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<EventLog>> call, Throwable t) {
                // Можно показать Toast или другую обработку ошибки
            }
        });
    }
}

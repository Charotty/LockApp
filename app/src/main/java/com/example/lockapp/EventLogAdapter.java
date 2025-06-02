package com.example.lockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class EventLogAdapter extends BaseAdapter {
    private Context context;
    private List<EventLog> events;

    public EventLogAdapter(Context context, List<EventLog> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        EventLog event = events.get(position);
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);
        text1.setText(event.fio + " (" + event.position + ") - " + event.method + ": " + event.result);
        text2.setText("Попытка: " + event.attempt + ", Время: " + event.timestamp);
        return convertView;
    }
}

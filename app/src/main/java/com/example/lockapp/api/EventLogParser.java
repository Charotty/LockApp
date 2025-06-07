package com.example.lockapp.api;

import com.example.lockapp.EventLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventLogParser {
    // Пример CSV: fio,position,method,result,attempt,timestamp\n...
    public static List<EventLog> parseCSV(String csv) {
        List<EventLog> logs = new ArrayList<>();
        String[] lines = csv.split("\n");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                logs.add(new EventLog(
                        parts[0], // fio
                        parts[1], // position
                        parts[2], // method
                        parts[3], // result
                        Integer.parseInt(parts[4]), // attempt
                        parts[5]  // timestamp
                ));
            }
        }
        return logs;
    }

    // Пример JSON: [ {fio:..., position:..., ...}, ... ]
    public static List<EventLog> parseJSON(String json) {
        List<EventLog> logs = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                logs.add(new EventLog(
                        obj.optString("fio"),
                        obj.optString("position"),
                        obj.optString("method"),
                        obj.optString("result"),
                        obj.optInt("attempt"),
                        obj.optString("timestamp")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logs;
    }
}

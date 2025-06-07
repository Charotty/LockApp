package com.example.lockapp.api;

import com.example.lockapp.User;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserParser {
    // CSV: id,fio,position,password,rfid,fingerprint,ivRfid,ivFingerprint\n...
    public static List<User> parseCSV(String csv) {
        List<User> users = new ArrayList<>();
        String[] lines = csv.split("\n");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 8) {
                users.add(new User(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5],
                        parts[6],
                        parts[7]
                ));
            }
        }
        return users;
    }

    // JSON: [ {id:..., fio:..., ...}, ... ]
    public static List<User> parseJSON(String json) {
        List<User> users = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                users.add(new User(
                        obj.optLong("id"),
                        obj.optString("fio"),
                        obj.optString("position"),
                        obj.optString("password"),
                        obj.optString("rfid"),
                        obj.optString("fingerprint"),
                        obj.optString("ivRfid"),
                        obj.optString("ivFingerprint")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static String toCSV(List<User> users) {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append(user.id).append(",")
              .append(user.fio).append(",")
              .append(user.position).append(",")
              .append(user.password).append(",")
              .append(user.rfidStr()).append(",")
              .append(user.fingerprintStr()).append(",")
              .append(user.ivRfidStr()).append(",")
              .append(user.ivFingerprintStr()).append("\n");
        }
        return sb.toString();
    }

    public static String toJSON(List<User> users) {
        JSONArray arr = new JSONArray();
        try {
            for (User user : users) {
                JSONObject obj = new JSONObject();
                obj.put("id", user.id);
                obj.put("fio", user.fio);
                obj.put("position", user.position);
                obj.put("password", user.password);
                obj.put("rfid", user.rfidStr());
                obj.put("fingerprint", user.fingerprintStr());
                obj.put("ivRfid", user.ivRfidStr());
                obj.put("ivFingerprint", user.ivFingerprintStr());
                arr.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arr.toString();
    }
}

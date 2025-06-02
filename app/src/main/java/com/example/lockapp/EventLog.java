package com.example.lockapp;

public class EventLog {
    public String fio;
    public String position;
    public String method;
    public String result;
    public int attempt;
    public String timestamp;

    public EventLog(String fio, String position, String method, String result, int attempt, String timestamp) {
        this.fio = fio;
        this.position = position;
        this.method = method;
        this.result = result;
        this.attempt = attempt;
        this.timestamp = timestamp;
    }
}

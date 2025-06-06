package com.example.lockapp;

public class User {
    public long id;
    public String fio;
    public String position;
    public String password;
    public String rfid;
    public String fingerprint;
    public String ivRfid;
    public String ivFingerprint;

    public User() {}

    public User(long id, String fio, String position, String password, String rfid, String fingerprint, String ivRfid, String ivFingerprint) {
        this.id = id;
        this.fio = fio;
        this.position = position;
        this.password = password;
        this.rfid = rfid;
        this.fingerprint = fingerprint;
        this.ivRfid = ivRfid;
        this.ivFingerprint = ivFingerprint;
    }

    public String rfidStr() { return rfid != null ? rfid : ""; }
    public String fingerprintStr() { return fingerprint != null ? fingerprint : ""; }
    public String ivRfidStr() { return ivRfid != null ? ivRfid : ""; }
    public String ivFingerprintStr() { return ivFingerprint != null ? ivFingerprint : ""; }
}

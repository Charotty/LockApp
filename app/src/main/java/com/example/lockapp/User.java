package com.example.lockapp;

public class User {
    public long id;
    public String fio;
    public String position;
    public String password;
    public byte[] rfid;
    public byte[] fingerprint;
    public byte[] ivRfid;
    public byte[] ivFingerprint;

    public User() {}

    public User(long id, String fio, String position, String password, byte[] rfid, byte[] fingerprint, byte[] ivRfid, byte[] ivFingerprint) {
        this.id = id;
        this.fio = fio;
        this.position = position;
        this.password = password;
        this.rfid = rfid;
        this.fingerprint = fingerprint;
        this.ivRfid = ivRfid;
        this.ivFingerprint = ivFingerprint;
    }

    public String rfidStr() { return rfid != null ? bytesToHex(rfid) : ""; }
    public String fingerprintStr() { return fingerprint != null ? bytesToHex(fingerprint) : ""; }
    public String ivRfidStr() { return ivRfid != null ? bytesToHex(ivRfid) : ""; }
    public String ivFingerprintStr() { return ivFingerprint != null ? bytesToHex(ivFingerprint) : ""; }
    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}

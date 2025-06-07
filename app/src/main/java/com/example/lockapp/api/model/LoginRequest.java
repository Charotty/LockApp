package com.example.lockapp.api.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("fio")
    private String fio;
    
    @SerializedName("password")
    private String password;

    public LoginRequest() {
        // Default constructor required for Gson
    }

    public LoginRequest(String fio, String password) {
        this.fio = fio;
        this.password = password;
    }

    public String getFio() { 
        return fio; 
    }
    
    public void setFio(String fio) { 
        this.fio = fio; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
}

package com.example.lockapp.api;

import com.example.lockapp.api.model.LoginRequest;
import com.example.lockapp.api.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);
}

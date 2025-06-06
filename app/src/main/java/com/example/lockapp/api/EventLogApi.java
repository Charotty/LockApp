package com.example.lockapp.api;

import com.example.lockapp.EventLog;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface EventLogApi {
    @GET("events")
    Call<List<EventLog>> getEvents();

    @POST("events")
    Call<EventLog> addEvent(@Body EventLog eventLog);
}

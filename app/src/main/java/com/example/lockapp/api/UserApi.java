package com.example.lockapp.api;

import com.example.lockapp.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/users")
    Call<List<User>> getUsers();

    @POST("/users")
    Call<User> addUser(@Body User user);

    @DELETE("/users/{id}")
    Call<Void> deleteUser(@Path("id") long id);
}

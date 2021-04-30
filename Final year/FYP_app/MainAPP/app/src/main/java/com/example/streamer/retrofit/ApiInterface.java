package com.example.streamer.retrofit;

import com.example.streamer.Models.Message;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("/fcm/send")
    Call<Message> sendMessage(@Header("Authorization") String token, @Body Message notification);

}

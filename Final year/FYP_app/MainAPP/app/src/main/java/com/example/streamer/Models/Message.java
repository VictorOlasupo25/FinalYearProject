package com.example.streamer.Models;

public class Message {
    String to;
    Notification data;

    public Message(String to, Notification data) {
        this.to = to;
        this.data = data;
    }

}
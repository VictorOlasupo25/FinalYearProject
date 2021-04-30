package com.example.streamer.Models;

public class ChatMessage {

    String message;
    String imageUrl;
    String audioUrl;
    String time;
    String senderId;
    String receiverId;



    public ChatMessage() {
    }


    public ChatMessage(String message, String imageUrl,
                       String audioUrl, String time, String senderId, String receiverId) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.time = time;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

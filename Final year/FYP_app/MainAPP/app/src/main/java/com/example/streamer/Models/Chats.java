package com.example.streamer.Models;

import java.util.List;

public class Chats {

    String chatId;
    String lastMessage;
    String lastImage;
    String lastAudio;
    List<String> usersList;


    public Chats() { }

    public Chats(String chatId, String lastMessage,
                 String lastImage, String lastAudio, List<String> usersList) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.lastImage = lastImage;
        this.lastAudio = lastAudio;
        this.usersList = usersList;
    }


    public String getLastAudio() {
        return lastAudio;
    }

    public void setLastAudio(String lastAudio) {
        this.lastAudio = lastAudio;
    }

    public String getLastImage() {
        return lastImage;
    }

    public void setLastImage(String lastImage) {
        this.lastImage = lastImage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<String> usersList) {
        this.usersList = usersList;
    }
}

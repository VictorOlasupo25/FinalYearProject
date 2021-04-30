package com.example.streamer.Models;

import java.util.List;

public class Post {
    String id;
    String caption;
    String imageUrl;
    String audioUrl;
    String userId;
    String time;
    List<String> likesList;


    public Post() { }

    public Post(String id, String caption, String imageUrl, String audioUrl,
                String userId, String time, List<String> likesList) {
        this.id = id;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.userId = userId;
        this.time = time;
        this.likesList = likesList;
    }


    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getLikesList() {
        return likesList;
    }

    public void setLikesList(List<String> likesList) {
        this.likesList = likesList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}

package com.example.streamer.Models;

public class User {

    String id, userName, email, phone,imageUrl,tokenId;
    boolean isTherapists;

public User(){ }

    public User(String id,String userName, String email,
                String phone, String imageUrl,boolean isTherapists,String tokenId) {

    this.id=id;
    this.userName = userName;
    this.email = email;
    this.phone = phone;
    this.imageUrl=imageUrl;
    this.isTherapists =isTherapists;
    this.tokenId=tokenId;
}


    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public boolean isTherapists() {
        return isTherapists;
    }

    public void setTherapists(boolean therapists) {
        isTherapists = therapists;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }






}

package com.sutmobiledev.bluetoothchat;

public class Message {
    private String type;
    private String name;
    private String imageAdd;
    private String body;
    private String photoAdress;

    boolean belongsToCurrentUser;
    Message(String type, String name, String body, String imageAdd, String  photoAdress, boolean belongsToCurrentUser){
        this.body = body;
        this.imageAdd = imageAdd;
        this.name = name;
        this.type = type;
        this.photoAdress = photoAdress;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return body;
    }

    public String getImageAdd() {
        return imageAdd;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPhotoAdress() {
        return photoAdress;
    }
}

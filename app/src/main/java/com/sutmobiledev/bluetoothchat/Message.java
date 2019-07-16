package com.sutmobiledev.bluetoothchat;

public class Message {
    public final static int TYPE_TEXT = 1;
    public final static int TYPE_IMAGE = 2;
    public final static int TYPE_VIDEO = 3;
    public final static int TYPE_VOICE = 4;
    public final static int TYPE_FILE = 5;


    private int type;
    private String name;
    private String imageAdd;
    private String body;
    private String photoAdress;

    boolean belongsToCurrentUser;
    Message(int type, String name, String body, String imageAdd, String  photoAdress, boolean belongsToCurrentUser){
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

    public int getType() {
        return type;
    }

    public String getPhotoAdress() {
        return photoAdress;
    }
}

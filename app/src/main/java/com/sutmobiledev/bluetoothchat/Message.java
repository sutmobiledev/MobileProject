package com.sutmobiledev.bluetoothchat;

public class Message {
    public final static int TYPE_TEXT = 1;
    public final static int TYPE_IMAGE = 2;
    public final static int TYPE_VIDEO = 3;
    public final static int TYPE_VOICE = 4;
    public final static int TYPE_FILE = 5;

    private int id;


    private int type;
    private String name;
    private String imageAdd;
    private String body;
    private String fileAddress;
    private int contactId;

    boolean belongsToCurrentUser;

    public Message(int type, String name, String body, String imageAdd, String photoAdress, int contactId, boolean belongsToCurrentUser){
        this.body = body;
        this.imageAdd = imageAdd;
        this.name = name;
        this.type = type;
        this.contactId = contactId;
        this.fileAddress = photoAdress;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public Message() {
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

    public String getBody() {
        return body;
    }

    public void setBelongsToCurrentUser(boolean belongsToCurrentUser) {
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public int getContactId() {
        return contactId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getType() {

        return type;
    }

    public String getTypeName(){
        switch (this.type) {
            case Message.TYPE_VIDEO :
                return "Video";
            case Message.TYPE_VOICE:
                return "Voice";
            case Message.TYPE_FILE:
                return "File";
            case Message.TYPE_TEXT:
                return "Text";
            case Message.TYPE_IMAGE:
                return "Image";
        }
        return null;
    }

    public String getFileAddress() {
        return fileAddress;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageAdd(String imageAdd) {
        this.imageAdd = imageAdd;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setFileAddress(String fileAddress) {
        this.fileAddress = fileAddress;
    }
}

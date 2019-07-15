package com.sutmobiledev.bluetoothchat;

class Chat {
    private String text;
    private Contact contact;
    private int chatID;
    private int isSent;
    private int isText;

    public Chat(String text, Contact contact, int chatID, int isSent, int isText) {
        this.text = text;
        this.contact = contact;
        this.chatID = chatID;
        this.isSent = isSent;
        this.isText = isText;
    }


    public Chat(){
    }

    public int getIsText() {
        return isText;
    }

    public void setIsText(int isText) {
        this.isText = isText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public int getChatID() {
        return chatID;
    }

    public void setChatID(int chatID) {
        this.chatID = chatID;
    }

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }
}

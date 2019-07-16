package com.sutmobiledev.bluetoothchat;

public class Contact {
    private int id;
    private String name;
    private String picAdd;

    public Contact() {
    }

    public Contact(int id, String name, String picAdd) {
        this.id = id;
        this.name = name;
        this.picAdd = picAdd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicAdd() {
        return picAdd;
    }

    public void setPicAdd(String picAdd) {
        this.picAdd = picAdd;
    }
}

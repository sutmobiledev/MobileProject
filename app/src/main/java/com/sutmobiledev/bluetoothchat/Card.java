package com.sutmobiledev.bluetoothchat;

public class Card {
    private String name;
    private Integer postId;
    private String imageAdd;


    public Card(String title_name,Integer postId,String imageAdd) {
        this.postId = postId;
        this.name = title_name;
        this.imageAdd = imageAdd;
    }

    public  Card(Contact contact){
        this.postId = contact.getId();
        this.name = contact.getName();
        this.imageAdd = contact.getPicAdd();
    }
    public String getName() {
        return name;
    }

    public Integer getPostId() {
        return postId;
    }

    public String getImageAdd() {
        return imageAdd;
    }
}


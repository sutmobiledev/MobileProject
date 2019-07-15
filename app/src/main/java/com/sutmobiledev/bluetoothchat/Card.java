package com.sutmobiledev.bluetoothchat;

public class Card {
    private String name;
    private Integer postId;


    public Card(String title_name,Integer postId) {
        this.postId = postId;
        this.name = title_name;
    }

    public String getName() {
        return name;
    }

    public Integer getPostId() {
        return postId;
    }
}


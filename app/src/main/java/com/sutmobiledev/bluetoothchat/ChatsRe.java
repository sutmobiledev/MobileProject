package com.sutmobiledev.bluetoothchat;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatsRe extends AppCompatActivity {
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    int postId;
    private String name;
    private String imageAdd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatsre);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.list);
        ArrayList<Message> messages = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                messageAdapter.add(message);
            }
        }
        Message message = new Message(Message.TYPE_TEXT,"mahsa","How are you doing? This is a long message that should probably wrap.","/sdcard/Download/annie-spratt-01Wa3tPoQQ8-unsplash.jpg","/sdcard/Download/annie-spratt-01Wa3tPoQQ8-unsplash.jpg", postId,false);
        messageAdapter.add(message);
        messagesView.setSelection(messagesView.getCount() - 1);
        messagesView.setAdapter(messageAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        postId = getSharedPreferences("postId", MODE_PRIVATE).getInt("postId", 0);
        name = getSharedPreferences("postId", MODE_PRIVATE).getString("contactName", "asghar");
        imageAdd = getSharedPreferences("PostId", MODE_PRIVATE).getString("contactName", "/sdcard/Download/annie-spratt-01Wa3tPoQQ8-unsplash.jpg");

    }
}

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
    DataBaseHelper db;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatsre);
        postId = getSharedPreferences("postId", MODE_PRIVATE).getInt("postId", 0);
        db = DataBaseHelper.getInstance(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.list);
        ArrayList<Message> messages = db.getMessages(postId);
        if (messages != null) {
            for (Message message : messages) {
                message.setImageAdd(db.getContact(postId).getPicAdd());
                messageAdapter.add(message);
            }
        }
        messagesView.setSelection(messagesView.getCount() - 1);
        messagesView.setAdapter(messageAdapter);
    }

}

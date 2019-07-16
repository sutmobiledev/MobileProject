package com.sutmobiledev.bluetoothchat;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ChatsRe extends AppCompatActivity {
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatsre);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.list);
        messagesView.setAdapter(messageAdapter);
    }
}

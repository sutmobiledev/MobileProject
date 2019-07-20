package com.sutmobiledev.bluetoothchat;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

public class ChatsRe extends AppCompatActivity {
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    ArrayList<Message> messages;
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
        messages = db.getMessages(postId);
        if (messages != null) {
            for (Message message : messages) {
                message.setImageAdd(db.getContact(postId).getPicAdd());
                message.setName(db.getContact(postId).getName());
//                Log.i("hereee", db.getContact(postId).getPicAdd());
                Log.i("hereee", db.getContact(postId).getName());
                Log.i("hereee", String.valueOf(db.getContact(postId).getId()));
                messageAdapter.add(message);
            }
        }
        messagesView.setSelection(messagesView.getCount() - 1);
        messagesView.setAdapter(messageAdapter);
        messagesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (messages.get(i).getType() == 4) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + messages.get(i).getName();
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(ChatsRe.this.getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(messages.get(i).getType() == 3){
                    VideoView videoView = (VideoView) messages.get(i).getMessageViewHolder().getSendedVideo();
                    MediaController mediaController = new MediaController(ChatsRe.this);
                    videoView.setMediaController(mediaController);
                    mediaController.setAnchorView(videoView);
//                    videoFragment = new VideoFragment();
//                    videoFragment.setMain(MainActivity.this);
//                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                    transaction.replace(R.id.frame, videoFragment);
//                    transaction.addToBackStack(null);
//                    transaction.commit();
//                    fr.setVisibility(View.VISIBLE);

                }
            }
        });
    }

}

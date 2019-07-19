package com.sutmobiledev.bluetoothchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.R;

import java.util.Timer;
import java.util.TimerTask;

public class EnterActivity extends Activity {
    RelativeLayout rl;
    DataBaseHelper db;
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DataBaseHelper.getInstance(this);
//        db.addMessage(new Message(1, "asghar", "ajksdflkal;sjdkfl;", null, null, 1, true));
        setContentView(R.layout.activity_enter);
        rl = findViewById(R.id.rootRL);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, ReviewActivity.class));
            }
        });
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(EnterActivity.this, ReviewActivity.class));
            }
        },2000);
    }

}

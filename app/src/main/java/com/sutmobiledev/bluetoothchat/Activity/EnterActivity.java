package com.sutmobiledev.bluetoothchat.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.Message;
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
//        db.addMessage(new Message(1, "akbar", "salam", null, null, 2, true));
//        db.addMessage(new Message(1, "akbar", "salam dg che ghad salam mikoni hamash", null, null, 2, false));
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
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}

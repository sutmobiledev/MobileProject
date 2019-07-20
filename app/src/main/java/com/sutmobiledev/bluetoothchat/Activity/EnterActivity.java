package com.sutmobiledev.bluetoothchat.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.sutmobiledev.bluetoothchat.Contact;
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
        db.addContact(new Contact(2,"akbar","/storage/emulated/0/Download/sana.jpg"));
//        db.addContact(new Contact(3,"ambar","/storage/emulated/0/Download/sana.jpg"));
//        db.addMessage(new Message(2, "akbar", "salam", "/storage/emulated/0/Download/sana.jpg", "/storage/emulated/0/Download/sana.jpg", 2, false));
        db.addMessage(new Message(3, "akbar", "salam dg che ghad salam mikoni hamash", null, Environment.getExternalStorageDirectory()+"/Movies/xperia_hd_landscapes.mp4", 2, false));
//        db.addMessage(new Message(4, "akbar", "salam dg che ghad salam mikoni hamash", null, "/storage/emulated/0/recording2.3pg", 2, false));
//        db.addMessage(new Message(5, "akbar", "salam dg che ghad salam mikoni hamash", null, "/storage/emulated/0/recording2.3pg", 2, false));
//        db.addMessage(new Message(1, "akbar", "salam dg che ghad salam mikoni hamash", null, "/storage/emulated/0/recording2.3pg", 2, false));

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
        },200);
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

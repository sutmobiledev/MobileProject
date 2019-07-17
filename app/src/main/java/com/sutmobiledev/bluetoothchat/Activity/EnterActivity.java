package com.sutmobiledev.bluetoothchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RelativeLayout;

import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.Message;
import com.sutmobiledev.bluetoothchat.R;

public class EnterActivity extends Activity {
    RelativeLayout rl;
    DataBaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DataBaseHelper.getInstance(this);
//        db.addMessage(new Message(1, "asghar", "ajksdflkal;sjdkfl;", null, null, 1, true));
        setContentView(R.layout.activity_enter);
        rl = findViewById(R.id.rootRL);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EnterActivity.this, ReviewActivity.class));
            }
        });
    }

}
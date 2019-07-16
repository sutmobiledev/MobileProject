package com.sutmobiledev.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RelativeLayout;

public class EnterActivity extends Activity {
    RelativeLayout rl;
    DataBaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DataBaseHelper.getInstance(this);
//        db.addMessage(new Message(1, "asghar", "ajksdflkal;sjdkfl;", null, null, 1, true));
        db.addMessage(new Message(1, "akbar", "salam", null, null, 2, false));
        db.addMessage(new Message(1, "akbar", "salam bar to ey hamneshine ghadimi toolani she payam ke bishtar az ye kah base moshkeli pish nayad ye vaght", null, null, 2, true));
        setContentView(R.layout.activity_enter);
        rl = (RelativeLayout) findViewById(R.id.rootRL);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EnterActivity.this, ChooseActivity.class));
            }
        });
    }

}

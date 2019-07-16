package com.sutmobiledev.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class EnterActivity extends Activity {
    RelativeLayout rl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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

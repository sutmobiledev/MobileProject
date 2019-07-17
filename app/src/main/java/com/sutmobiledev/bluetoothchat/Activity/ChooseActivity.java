package com.sutmobiledev.bluetoothchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.R;
import com.sutmobiledev.bluetoothchat.User;

public class ChooseActivity extends Activity {
    private Button btn_Set;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_choose);
        btn_Set = findViewById(R.id.btn_ok);
        editText = findViewById(R.id.edit_text);
        btn_Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(ChooseActivity.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    getSharedPreferences("post", MODE_PRIVATE).edit().putString("USER_NAME",editText.getText().toString()).apply();
                    startActivity(new Intent(ChooseActivity.this, ReviewActivity.class));
                }
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}

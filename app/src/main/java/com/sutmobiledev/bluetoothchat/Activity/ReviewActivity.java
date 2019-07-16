package com.sutmobiledev.bluetoothchat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sutmobiledev.bluetoothchat.Card;
import com.sutmobiledev.bluetoothchat.ChatsRe;
import com.sutmobiledev.bluetoothchat.Contact;
import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.ImageAdapter;
import com.sutmobiledev.bluetoothchat.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity{
    private List<Card> cards = new ArrayList<>();
    private DataBaseHelper dataBaseHelper;
    private ListView listView;
    private ViewStub stubList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = DataBaseHelper.getInstance(this);

        setContentView(R.layout.activity_review);
        listView = findViewById(R.id.l);
        ArrayList<Contact> contacts = dataBaseHelper.getContacts();
        if (contacts != null) {
            for (int i = 0; i < contacts.size(); i++) {
                cards.add(new Card(contacts.get(i)));

            }
        }
        stubList = findViewById(R.id.stub);
        stubList.inflate();
        stubList.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.l);
        listView.setAdapter(new ImageAdapter(this, R.layout.list_view, cards));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ReviewActivity.this.getSharedPreferences("postId", MODE_PRIVATE).edit().putInt("postId", cards.get(i).getPostId()).apply();
                Log.i("havaye gerye ba to",cards.get(i).getPostId().toString());
                ReviewActivity.this.startActivity(new Intent(ReviewActivity.this, ChatsRe.class));
            }
        });

    }

}

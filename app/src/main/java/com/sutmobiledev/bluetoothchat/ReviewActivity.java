package com.sutmobiledev.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private List<Card> cards = new ArrayList<>();
    private DataBaseHelper dataBaseHelper;
    private ListView listView;
    private ViewStub stubList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new DataBaseHelper(this);

        setContentView(R.layout.activity_review);
        listView = (ListView) findViewById(R.id.l);
        ArrayList<Contact> contacts = dataBaseHelper.getContacts();
        for (int i = 0; i < contacts.size(); i++) {
            cards.add(new Card(contacts.get(i)));

        }
        stubList = (ViewStub) findViewById(R.id.stub);
        stubList.inflate();
        stubList.setVisibility(View.VISIBLE);
        listView = (ListView) findViewById(R.id.l);
        listView.setAdapter(new ImageAdapter(this, R.layout.list_view, cards));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ReviewActivity.this.getSharedPreferences("postId", MODE_PRIVATE).edit().putInt("postId", cards.get(i).getPostId()).apply();
                ReviewActivity.this.getSharedPreferences("postId", MODE_PRIVATE).edit().putString("contactName", cards.get(i).getName()).apply();
                ReviewActivity.this.getSharedPreferences("postId", MODE_PRIVATE).edit().putString("imageAdd", cards.get(i).getImageAdd()).apply();

                ReviewActivity.this.startActivity(new Intent(ReviewActivity.this, ChatsRe.class));
            }
        });

    }
}

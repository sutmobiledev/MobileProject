package com.sutmobiledev.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private List<Card> cards = new ArrayList<>();

    private ListView listView;
    private ViewStub stubList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);
        listView = findViewById(R.id.l);
        int i = 0;
        while(i<10){
            cards.add(new Card("salam",90));
            i++;
        }
        stubList = findViewById(R.id.stub);
        stubList.inflate();
        stubList.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.l);
        listView.setAdapter(new ImageAdapter(this, R.layout.list_view, cards));
    }
}

package com.sutmobiledev.bluetoothchat.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Card;
import com.sutmobiledev.bluetoothchat.ChatsRe;
import com.sutmobiledev.bluetoothchat.ChooseImage;
import com.sutmobiledev.bluetoothchat.Contact;
import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.ImageAdapter;
import com.sutmobiledev.bluetoothchat.R;
import com.sutmobiledev.bluetoothchat.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private List<Card> cards = new ArrayList<>();
    private DataBaseHelper dataBaseHelper;
    private ListView listView;
    private ViewStub stubList;
    ImageView profilePhoto;
    TextView nameTextView;
    ChooseImage chooseImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = DataBaseHelper.getInstance(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        chooseImage = new ChooseImage();
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


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        profilePhoto = (ImageView) header.findViewById(R.id.imageView1);
        nameTextView = (TextView) header.findViewById(R.id.user);
        if (getSharedPreferences("post",MODE_PRIVATE).contains("USER_NAME")) {
            Log.i("HEREEEEE",getSharedPreferences("post", MODE_PRIVATE).getString("USER_NAME", "Unknown"));
            User.user_name = getSharedPreferences("post", MODE_PRIVATE).getString("USER_NAME", "Unknown");
        } else {
            getSharedPreferences("post", MODE_PRIVATE).edit().putString("USER_NAME",User.user_name).apply();
        }
        if (getSharedPreferences("post",MODE_PRIVATE).contains("PROFILE_PIC")) {
            User.profileAddress = getSharedPreferences("post",MODE_PRIVATE).getString("PROFILE_PIC",null);
        } else {
            getSharedPreferences("post",MODE_PRIVATE).edit().putString("PROFILE_PIC", User.profileAddress).apply();
        }
        nameTextView.setText(User.user_name);
        if(User.getProfileAddress()!= null) {
            File folder2 = new File(User.getProfileAddress());
            if (folder2.exists()) {
                String folderpath3 = folder2.getAbsolutePath().toString().trim();
                profilePhoto.setImageBitmap(BitmapFactory.decodeFile(folderpath3));
            } else {
                Log.e("Hereee", "image not exists");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Chat) {
            startActivity(new Intent(ReviewActivity.this, MainActivity.class));
        } else if (id == R.id.Review) {

        } else if (id == R.id.ChangeUsername) {
            startActivity(new Intent(ReviewActivity.this, ChooseActivity.class));

        } else if (id == R.id.ChangePhoto) {
            chooseImage.choosePhotoFromGallary(ReviewActivity.this);


        } else if (id == R.id.Appearence) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == 1) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = chooseImage.saveImage(bitmap);
                    Toast.makeText(ReviewActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    profilePhoto.setImageBitmap(bitmap);
                    User.profileAddress = path;
                    getSharedPreferences("post",MODE_PRIVATE).edit().putString("PROFILE_PIC", User.profileAddress).apply();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ReviewActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        };
    }
}

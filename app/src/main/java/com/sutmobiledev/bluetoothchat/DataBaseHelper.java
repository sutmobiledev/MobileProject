package com.sutmobiledev.bluetoothchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;

    public DataBaseHelper(Context context) {
        super(context, "DB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE CONTACTS (ID INTEGER PRIMARY KEY, NAME TEXT, PICADD TEXT) ");
        db.execSQL("CREATE TABLE CHATS (ID INTEGER PRIMARY KEY AUTOINCREMENT, TEXT TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS CONTACTS");
        db.execSQL("DROP TABLE IF EXISTS CHATS");
        onCreate(db);
    }

    public void addContact(Contact contact) {
        this.db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID", contact.getId());
        contentValues.put("NAME", contact.getName());
        contentValues.put("PICADD", contact.getPicAdd());
        this.db.insert("CONTACTS", null, contentValues);
        this.db.close();
    }


    public void addChat(Chat chat) {
        this.db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CHATID", chat.getChatID());
        contentValues.put("ISTEXT", chat.getIsText());
        contentValues.put("TEXT", chat.getText());
        contentValues.put("ISSENT", chat.getIsSent());
        this.db.insert("CHAT" + chat.getContact().getId(), null, contentValues);
        this.db.close();

    }

    public ArrayList<Chat> getchats(Contact contact) {
        this.db = this.getReadableDatabase();
        ArrayList<Chat> chats = null;
        Cursor cursor = db.rawQuery("SELECT * FROM CHATS WHERE ID = " + contact.getId(), null);
        if (cursor.moveToFirst()) {
            chats = new ArrayList<>();
            do {
                Chat chat = new Chat();
                chat.setChatID(cursor.getInt(0));
                chat.setIsText(cursor.getInt(1));
                chat.setText(cursor.getString(2));
                chat.setIsSent(cursor.getInt(3));
                chats.add(chat);
            } while (cursor.moveToNext());

        }
        db.close();
        cursor.close();
        return chats;
    }

    public ArrayList<Contact> getContacts() {
        this.db = this.getReadableDatabase();
        ArrayList<Contact> contacts = null;
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS", null);
        if (cursor.moveToFirst()) {
            contacts = new ArrayList<>();
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(0));
                contact.setName(cursor.getString(1));
                contact.setPicAdd(cursor.getString(2));

                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contacts;
    }
}


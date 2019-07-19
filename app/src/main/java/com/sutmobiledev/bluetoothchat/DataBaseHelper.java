package com.sutmobiledev.bluetoothchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "contacts_messages_db";

    SQLiteDatabase db;

    private static DataBaseHelper dataBaseHelper;
    private DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        dataBaseHelper = this;
    }
    public static DataBaseHelper getInstance(Context context){
        if (dataBaseHelper == null) {
            return new DataBaseHelper(context.getApplicationContext());
        }
        return dataBaseHelper;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE CONTACTS (ID INTEGER PRIMARY KEY, NAME TEXT, PICADD TEXT) ");
        db.execSQL("CREATE TABLE MESSAGES (ID INTEGER PRIMARY KEY AUTOINCREMENT, CONTACTID INTEGER ,TYPE INTEGER,BODY TEXT, BELONGSTOCURRENTUSER INTEGER)");
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


    public void addMessage(Message message) {
        this.db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CONTACTID", message.getContactId());
        contentValues.put("TYPE", message.getType());
        if(message.getType() == Message.TYPE_TEXT) {
            contentValues.put("BODY", message.getBody());
        }else {
            contentValues.put("BODY", message.getFileAddress());
        }
        contentValues.put("BELONGSTOCURRENTUSER", message.belongsToCurrentUser ? 1 : 0);
        this.db.insert("MESSAGES", null, contentValues);
        Log.i("dododo","db is EZ");
        this.db.close();

    }


    public ArrayList<Message> getMessages(int contactId) {
        this.db = this.getReadableDatabase();
        ArrayList<Message> messages = null;
        Cursor cursor = db.rawQuery("SELECT * FROM MESSAGES WHERE CONTACTID ='"+contactId +"'" , null);
        if (cursor.moveToFirst()) {
            Log.i("hi", "hello");
            messages = new ArrayList<>();
            do {
                Message message = new Message();
                message.setId(cursor.getInt(0));
                message.setContactId(cursor.getInt(1));
                message.setType(cursor.getInt(2));
                if (message.getType() == Message.TYPE_TEXT) {
                    message.setBody(cursor.getString(3));
                } else {
                    message.setFileAddress(cursor.getString(3));
                }
                message.setBelongsToCurrentUser(cursor.getInt(4) == 1);
                messages.add(message);
            } while (cursor.moveToNext());

        }
        db.close();
        cursor.close();
        Log.i("is db working corect?", "EZ");
        return messages;
    }

    public ArrayList<Contact> getContacts() {
        this.db = this.getReadableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
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

    public Contact getContact(int contactID) {
        this.db = this.getReadableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE ID='" +contactID+"'" , null);
        if (cursor.moveToFirst()) {
            Log.i("salam","salam");
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
        return contacts.get(0);
    }


    public Boolean firsConnection(int contactId) {
        this.db = this.getReadableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE ID='" +contactId+"'" , null);
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;


    }
}


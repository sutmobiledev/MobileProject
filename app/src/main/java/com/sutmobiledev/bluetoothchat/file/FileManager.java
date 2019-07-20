package com.sutmobiledev.bluetoothchat.file;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;
import com.sutmobiledev.bluetoothchat.ChatController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileManager {
    private static final FileManager ourInstance = new FileManager();
    private MainActivity mainActivity;
    private Handler handler;
    private MediaRecorder audioRecorder;
    public static final int BUFFER_SIZE = 512;

    public static final String TAG = "SUTBluetoothFileManager";

    private FileManager() {
        /*audioRecorder = new MediaRecorder();
        audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);*/
    }

    public static FileManager getInstance() {
        return ourInstance;
    }

    public FileManager init(MainActivity mainActivity, Handler handler) {
        this.mainActivity = mainActivity;
        this.handler = handler;
        return this;
    }

    public void showFileChooser() {
//        if (mainActivity.chatController.getState() != ChatController.STATE_CONNECTED) {
//            Toast.makeText(mainActivity, "Connection was lost!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(mainActivity, "Can't access to storage. Access Denied!!", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        try {
            mainActivity.startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    MainActivity.CHOOSE_FILE);
//            mainActivity.startActivityForResult(galleryIntent, MainActivity.CHOOSE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "showFileChooser: error = " + ex.getMessage());
            Toast.makeText(mainActivity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void recorderAudioMessage() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.RECORD_AUDIO}, Manifest.permission.RECORD_AUDIO.hashCode());

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(mainActivity, "Can't record audio. Access Denied!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void recorderVideoMessage() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA}, Manifest.permission.CAMERA.hashCode());

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(mainActivity, "Can't record video. Access Denied!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendFile(Uri uri, int type) {
//        if (mainActivity.chatController.getState() != ChatController.STATE_CONNECTED) {
//            Toast.makeText(mainActivity, "Connection was lost!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        InputStream fin = null;
        String name = "";
        long size = 0;

        Log.d(TAG, "sendFile: " + uri.getAuthority());
        try {
            // external storage
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                fin = mainActivity.getContentResolver().openInputStream(uri);

                size = fin.available();

                name = DocumentsContract.getDocumentId(uri).split(":")[1];
                String[] splited = name.split("/");
                name = splited[splited.length - 1];
            }
            // downloads
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                fin = mainActivity.getContentResolver().openInputStream(uri);

                size = fin.available();

                String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor = mainActivity.getContentResolver().query(uri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                cursor.moveToFirst();

                Log.d(TAG, "sendFile: download : " + cursor.getString(column_index));
                name = cursor.getString(column_index);
            }
            // media
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                fin = mainActivity.getContentResolver().openInputStream(uri);

                size = fin.available();

                // finding file path
                String file_type = DocumentsContract.getDocumentId(uri).split(":")[0];
                Uri contentUri;
                if ("image".equals(file_type))
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else if ("video".equals(file_type))
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else if ("audio".equals(file_type))
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                else {
                    Log.e(TAG, "sendFile: Not Supported");
                    Toast.makeText(mainActivity, "This file explorer is not supported.", Toast.LENGTH_LONG).show();
                    return;
                }
                String[] projection = {MediaStore.Images.Media.DATA}, selectionArgs = {DocumentsContract.getDocumentId(uri).split(":")[1]};
                Cursor cursor = mainActivity.getContentResolver().query(contentUri,
                        projection,
                        "_id=?",
                        selectionArgs,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                String[] temp = cursor.getString(column_index).split("/");
                name = temp[temp.length - 1];
            }
            // open by other file managers
            else if (uri.getPath().startsWith("/storage/emulated/0")) {
                File file = new File(uri.getPath());
                fin = new FileInputStream(file);

                size = file.length();

                String[] temp = uri.getPath().split("/");
                name = temp[temp.length - 1];
            } else if ("media".equals(uri.getAuthority())) {
                fin = mainActivity.getContentResolver().openInputStream(uri);

                size = fin.available();

                // path
                String media_type = mainActivity.getContentResolver().getType(uri).split("/")[0];
                Log.d(TAG, "sendFile: type = " + media_type);

                String[] proj = {"_data"};
                Cursor cursor = mainActivity.getContentResolver().query(uri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
                int column_index = cursor.getColumnIndexOrThrow("_data");
                cursor.moveToFirst();

                String[] temp = cursor.getString(column_index).split("/");
                name = temp[temp.length - 1];
            } else {
                Log.e(TAG, "sendFile: Not Supported");
                Toast.makeText(mainActivity, "This file explorer is not supported.", Toast.LENGTH_LONG).show();
                return;
            }

            if (size > 0) {
                //handler.obtainMessage(MainActivity.MESSAGE_FILE_SEND, type,0,file_name).sendToTarget();

                // Message type
                byte[] send = ChatController.SEND_FILE.getBytes();
                mainActivity.chatController.write(send, 0);
                Log.d(TAG, "sendFile: msg = " + new String(send));

                // length
                send = String.valueOf(size).getBytes();
                mainActivity.chatController.write(send, 0);
                Log.d(TAG, "sendFile: len = " + new String(send));

                // file type
                send = String.valueOf(type).getBytes();
                mainActivity.chatController.write(send, 0);
                Log.d(TAG, "sendFile: type = " + new String(send));

                // file name
                send = name.getBytes();
                mainActivity.chatController.write(send, 0);
                Log.d(TAG, "sendFile: name = " + new String(send));

                // content
                try {
                    send = new byte[BUFFER_SIZE];
                    int bytecnt = 0, tot = 0;
                    while ((bytecnt = fin.read(send)) != -1) {
                        tot += bytecnt;
                        mainActivity.chatController.write(send, 0);
                        Log.i(TAG, "sendFile: bytecnt = " + String.valueOf(bytecnt));
                        Log.e(TAG, "sendFile: str = " + new String(send));

                        send = new byte[BUFFER_SIZE];
                    }
                    Log.i(TAG, "sendFile: tot = " + String.valueOf(tot));
                    assert tot == size;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "sendFile: " + e.getMessage());
        }
    }
}

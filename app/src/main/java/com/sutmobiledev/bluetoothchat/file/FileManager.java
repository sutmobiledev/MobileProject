package com.sutmobiledev.bluetoothchat.file;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;
import com.sutmobiledev.bluetoothchat.ChatController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileManager {
    private static final FileManager ourInstance = new FileManager();
    private MainActivity mainActivity;
    private Handler handler;

    private FileManager() {
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
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Manifest.permission.WRITE_EXTERNAL_STORAGE.hashCode());

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            mainActivity.startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    MainActivity.CHOOSE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(mainActivity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void recorderAudioMessage() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.RECORD_AUDIO}, Manifest.permission.RECORD_AUDIO.hashCode());

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(mainActivity, "Can't record audio. Access Denied!!", Toast.LENGTH_SHORT);
        }
    }

    public void recorderVideoMessage() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA}, Manifest.permission.CAMERA.hashCode());


    }

    public void sendFile(String file_name, String type) {
        if (mainActivity.chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(mainActivity, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        File out = new File(file_name);
        if (out.length() > 0) {
            String[] obj = new String[2];
            obj[0] = file_name;
            obj[1] = type;

            handler.obtainMessage(MainActivity.MESSAGE_FILE_SEND, obj).sendToTarget();

            // Message type
            byte[] send = ChatController.SEND_FILE.getBytes();
            mainActivity.chatController.write(send, 0);

            // length
            send = String.valueOf(out.length()).getBytes();
            mainActivity.chatController.write(send, 0);

            // file type
            send = type.getBytes();
            mainActivity.chatController.write(send, 0);

            // file name
            String[] temp = file_name.split("/");
            send = temp[temp.length - 1].getBytes();
            mainActivity.chatController.write(send, 0);

            // content
            try {
                send = new byte[8 * 1024];
                FileInputStream fin = new FileInputStream(out);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fin, 8 * 1024);

                while ((bufferedInputStream.read(send)) != -1)
                    mainActivity.chatController.write(send, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.sutmobiledev.bluetoothchat;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordAudio extends Fragment implements View.OnClickListener {
    MainActivity main;
    private Button record, stop, play,ok;
    private MediaRecorder mediaRecorder;
    private String outputFile;
    int number = 0;
    public RecordAudio() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.audio_record, container, false);

        play = rootView.findViewById(R.id.play);
        record = rootView.findViewById(R.id.record);
        stop = rootView.findViewById(R.id.stop);
        ok = rootView.findViewById(R.id.ok);
        number = main.getSharedPreferences("post", Context.MODE_PRIVATE).getInt("REC_NUM", 0);
        stop.setEnabled(false);
        play.setEnabled(false);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/recording"+String.valueOf(number)+".3pg";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(outputFile);
        play.setOnClickListener(this);
        record.setOnClickListener(this);
        stop.setOnClickListener(this);
        ok.setOnClickListener(this);

        return rootView;
    }


    public void setMain(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.record:
//                main.chooseImage.choosePhotoFromGallery(main);
                number = main.getSharedPreferences("post", Context.MODE_PRIVATE).getInt("REC_NUM", 0);
                try {
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaRecorder.start();
                record.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(main.getApplicationContext(),"Recording started",Toast.LENGTH_LONG).show();
                break;

            case R.id.stop:
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                Toast.makeText(main.getApplicationContext(),"Audio Recorder Successful",Toast.LENGTH_LONG).show();
//                main.chooseImage.choosePhotoFromGallery(main);
                break;
            case R.id.play:
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(main.getApplicationContext(),"Playing Audio",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ok:
                main.fileManager.sendFile(Uri.fromFile(new File(outputFile)), 4);
                number += 1;
                main.getSharedPreferences("post", Context.MODE_PRIVATE).edit().putInt("REC_NUM",number).apply();
                main.fr.setVisibility(View.GONE);
//                main.fileManager.showFileChooser();
                break;
        }
    }


}
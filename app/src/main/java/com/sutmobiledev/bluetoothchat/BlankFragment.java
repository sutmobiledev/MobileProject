package com.sutmobiledev.bluetoothchat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment implements View.OnClickListener {
    MainActivity main;
    public BlankFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        Button image = rootView.findViewById(R.id.btn_image);
        Button video = rootView.findViewById(R.id.btn_video);
        Button voice = rootView.findViewById(R.id.btn_voice);
        Button file = rootView.findViewById(R.id.btn_file);

        image.setOnClickListener(this);
        video.setOnClickListener(this);
        voice.setOnClickListener(this);
        file.setOnClickListener(this);


        return rootView;
    }

    public void setMain(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.btn_image:
                main.chooseImage.showPictureDialog(main);
                break;

            case R.id.btn_video:
                main.chooseVideo.chooseVideoFromGallary(main);
                break;
            case R.id.btn_voice:
                main.recordAudio = new RecordAudio();
                main.recordAudio.setMain(main);
                FragmentTransaction transaction = main.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, this);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btn_file:
                main.fileManager.showFileChooser();
                break;
        }
    }


}
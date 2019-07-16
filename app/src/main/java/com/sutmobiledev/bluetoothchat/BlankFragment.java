package com.sutmobiledev.bluetoothchat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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

        Button image = (Button) rootView.findViewById(R.id.btn_image);
        Button video = (Button) rootView.findViewById(R.id.btn_video);
        Button voice = (Button) rootView.findViewById(R.id.btn_voice);
        Button file = (Button) rootView.findViewById(R.id.btn_file);

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
                break;

            case R.id.btn_video:
                break;
            case R.id.btn_voice:
                break;
            case R.id.btn_file:
                main.showFileChooser();
                break;
        }
    }


}
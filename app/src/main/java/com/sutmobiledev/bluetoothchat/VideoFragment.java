package com.sutmobiledev.bluetoothchat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements View.OnClickListener {
    MainActivity main;
    VideoView videoView;
    public VideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        videoView = rootView.findViewById(R.id.video_view);

        MediaController mediaController = new MediaController(main);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);



        return rootView;
    }

    public void setMain(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onClick(View view) {
    }


}

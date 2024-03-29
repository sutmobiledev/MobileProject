package com.sutmobiledev.bluetoothchat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
    AppCompatActivity main;
    VideoView videoView;
    String videoPath;
    public VideoFragment() {
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.videoplay_fram, container, false);

        videoView = rootView.findViewById(R.id.video_view);
        videoView.setVideoURI(Uri.parse(videoPath));

        MediaController mediaController = new MediaController(main);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);



        return rootView;
    }

    public void setMain(AppCompatActivity main) {
        this.main = main;
    }

    @Override
    public void onClick(View view) {
    }


}

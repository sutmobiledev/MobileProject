package com.sutmobiledev.bluetoothchat;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


public class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
    public ImageView sendedPhoto;
    public VideoView sendedVideo;

    public VideoView getSendedVideo() {
        return sendedVideo;
    }
}
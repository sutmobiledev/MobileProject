package com.sutmobiledev.bluetoothchat;

import android.location.Address;
import android.widget.ImageView;
import android.widget.TextView;

// Your "view holder" that holds references to each subview
class ViewHolder {
    TextView nameTextView;
    final TextView Id;
    ImageView imageView;


    public ViewHolder(TextView nameTextView, TextView id, ImageView imageViewAdd) {
        this.nameTextView = nameTextView;
        this.Id = id;

    }
}
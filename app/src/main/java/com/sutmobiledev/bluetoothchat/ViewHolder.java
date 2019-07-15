package com.sutmobiledev.bluetoothchat;

import android.widget.TextView;

// Your "view holder" that holds references to each subview
class ViewHolder {
    final TextView nameTextView;
    final TextView Id;


    public ViewHolder(TextView nameTextView, TextView id) {
        this.nameTextView = nameTextView;
        this.Id = id;
    }
}
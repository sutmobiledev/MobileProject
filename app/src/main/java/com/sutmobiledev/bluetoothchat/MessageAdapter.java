package com.sutmobiledev.bluetoothchat;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter{
    List<Message> messages = new ArrayList<Message>();
    Context context;



    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);
        message.setMessageViewHolder(holder);

        if (message.isBelongsToCurrentUser()) {
            if(message.getType() == Message.TYPE_IMAGE){
                convertView = messageInflater.inflate(R.layout.my_message_image, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                File folder = new File(message.getFileAddress());
//            File   folderpath = new File(folder+File.separator+imagename);
                if(folder.exists())
                {
                    String folderpath1 = folder.getAbsolutePath().toString().trim();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                }
                else
                {
                    Log.e("Hereee","image not exists");
                }
                holder.sendedPhoto = imageView;
                convertView.setTag(holder);
            }
            else if(message.getType() == Message.TYPE_VIDEO){
                convertView = messageInflater.inflate(R.layout.my_message_video2, null);
//                VideoView videoView = convertView.findViewById(R.id.video_view);
//                if(message.getFileAddress()!= null) {
//                    String videoPath = message.getFileAddress();
////            File   folderpath = new File(folder+File.separator+imagename);
//                    Uri uri = Uri.parse(videoPath);
//                    videoView.setVideoURI(uri);
//                }
//                holder.sendedVideo = videoView;
                convertView.setTag(holder);
            }
            else if (message.getType() == Message.TYPE_TEXT){
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);
                holder.messageBody.setText(message.getText());
            }
            else if (message.getType() == Message.TYPE_VOICE){
                convertView = messageInflater.inflate(R.layout.my_message_voice, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.messageBody.setText(message.getTypeName());
                convertView.setTag(holder);
            }
            else if (message.getType() == Message.TYPE_FILE ){
                convertView = messageInflater.inflate(R.layout.my_message_file, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.messageBody.setText(message.getTypeName());
                convertView.setTag(holder);
            }
        } else {
            if(message.getType() == Message.TYPE_IMAGE){
                convertView = messageInflater.inflate(R.layout.their_message_image, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                if(message.getImageAdd() != null){
                File folder = new File(message.getImageAdd());
                if (folder.exists()) {
                    String folderpath1 = folder.getAbsolutePath().trim();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                } else {
                    Log.e("Hereee", "image not exists");
                }
                }
                holder.avatar = imageView;
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setText(message.getName());
                ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imageView2);
                if(message.getFileAddress() != null){
                File folder2 = new File(message.getFileAddress());
                Log.i("imageee",message.getFileAddress());
                if(folder2.exists())
                {
                    String folderpath3 = folder2.getAbsolutePath().toString().trim();
                    imageView2.setImageBitmap(BitmapFactory.decodeFile(folderpath3));
                    Log.i("imageee", "exists");
                }
                else
                {
                    Log.e("Hereee","image not exists");
                }}
                holder.sendedPhoto = imageView2;
                convertView.setTag(holder);
            }
            else if(message.getType() == Message.TYPE_VIDEO){

                convertView = messageInflater.inflate(R.layout.their_message_video2, null);
                ImageView imageView = convertView.findViewById(R.id.imageView1);

                if(message.getImageAdd() != null){
                    File folder = new File(message.getImageAdd());
                    if (folder.exists()) {
                        String folderpath1 = folder.getAbsolutePath().toString().trim();
                        imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                    } else {
                        Log.e("Hereee", "image not exists");
                    }
                }
                holder.avatar = imageView;
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setText(message.getName());
//                VideoView videoView = convertView.findViewById(R.id.video_view);
//                if(message.getFileAddress()!= null) {
//                    String videoPath = message.getFileAddress();
//                    Log.i("pathhhh",videoPath);
////            File   folderpath = new File(folder+File.separator+imagename);
////                    Uri uri = Uri.parse(videoPath);
//                    videoView.setVideoURI(Uri.parse(videoPath));
//                }
//                holder.sendedVideo = videoView;
                convertView.setTag(holder);
            }
            else if(message.getType() == Message.TYPE_TEXT){
                convertView = messageInflater.inflate(R.layout.their_message, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                if(message.getImageAdd() != null){
                File folder = new File(message.getImageAdd());
                if (folder.exists()) {
                    String folderpath1 = folder.getAbsolutePath().toString().trim();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                } else {
                    Log.e("Hereee", "image not exists");
                }
                }
                holder.avatar = imageView;
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.name.setText(message.getName());
                holder.messageBody.setText(message.getText());
                convertView.setTag(holder);
            }
            else if(message.getType() == Message.TYPE_VOICE){
                convertView = messageInflater.inflate(R.layout.their_message_voice, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                if(message.getImageAdd() != null){
                    File folder = new File(message.getImageAdd());
                    if (folder.exists()) {
                        String folderpath1 = folder.getAbsolutePath().toString().trim();
                        imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                    } else {
                        Log.e("Hereee", "image not exists");
                    }
                }
                holder.avatar = imageView;
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.name.setText(message.getName());
                holder.messageBody.setText(message.getTypeName());
                convertView.setTag(holder);
            }
            else if(message.getType() == Message.TYPE_FILE){
                convertView = messageInflater.inflate(R.layout.their_message_file, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                if(message.getImageAdd() != null){
                File folder = new File(message.getImageAdd());
                if (folder.exists()) {
                    String folderpath1 = folder.getAbsolutePath().toString().trim();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
                } else {
                    Log.e("Hereee", "image not exists");
                }}
                holder.avatar = imageView;
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.name.setText(message.getName());
                holder.messageBody.setText(message.getTypeName());
                convertView.setTag(holder);
            }
        }

        return convertView;
    }
}

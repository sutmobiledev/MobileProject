package com.sutmobiledev.bluetoothchat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class ImageAdapter extends ArrayAdapter<Card> {


    public ImageAdapter(Context context, int resource, List<Card> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        Card card = getItem(i);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.activity_card, null);

            final TextView nameTextView = view.findViewById(R.id.text1);
            final TextView authorTextView = view.findViewById(R.id.textview_book_author);
            final ImageView imageView = view.findViewWithTag(R.id.imageView1);
            final ViewHolder viewHolder = new ViewHolder(nameTextView, authorTextView,imageView);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameTextView.setText(card.getName());
        viewHolder.Id.setText(Integer.toString(card.getPostId()));

        ImageView imageView2 = view.findViewById(R.id.imageView1);
        if(card.getImageAdd() != null) {
            File folder = new File(card.getImageAdd());
//            File   folderpath = new File(folder+File.separator+imagename);
            if (folder.exists()) {
                String folderpath1 = folder.getAbsolutePath().toString().trim();
                imageView2.setImageBitmap(BitmapFactory.decodeFile(folderpath1));
            } else {
                Log.e("Hereee", "image not exists");
            }
            viewHolder.imageView = imageView2;
        }
        return view;
    }


}
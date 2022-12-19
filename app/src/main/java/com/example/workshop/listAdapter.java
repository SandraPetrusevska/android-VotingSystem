package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class listAdapter extends BaseAdapter {
    Context context;
    ArrayList <String> listPolls;
    int lImage;
    LayoutInflater inflater;

    public listAdapter(Context ctx, ArrayList <String> polls, int image ) {
        this.context = ctx;
        this.listPolls = polls;
        this.lImage = image;
        inflater = LayoutInflater.from(ctx);

    }

    @Override
    public int getCount() {
        return listPolls.size();
    }

    @Override
    public Object getItem(int position) {
        return listPolls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.list_row, null);
       // TextView txtView = (TextView) convertView.findViewById(R.id.pollName);
       // ImageView pollImg = (ImageView) convertView.findViewById(R.id.imagePoll);
     //   txtView.setText(listPolls.indexOf(position)) ;
       // pollImg.setImageResource(lImage);
        return convertView;
    }
}

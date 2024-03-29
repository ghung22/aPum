package com.hcmus.apum.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcmus.apum.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.hcmus.apum.MainActivity.debugEnabled;
import static com.hcmus.apum.MainActivity.mediaManager;

public class AlbumAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> mediaList;
    private final ArrayList<Integer> mediaCountList;

    public AlbumAdapter(Context context, ArrayList<String> mediaList, ArrayList<Integer> mediaCountList) {
        this.context = context;
        this.mediaList = mediaList;
        this.mediaCountList = mediaCountList;
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mediaList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    @SuppressLint({"ViewHolder", "InflateParams"})
    @SuppressWarnings("Duplicates")
    public View getView(int pos, View convertView, ViewGroup parent) {
        // Get elements
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.layout_visual_listview, null);  // Preview popup
        ImageView visual = row.findViewById(R.id.visual);
        TextView visualTitle = row.findViewById(R.id.visualTitle);
        TextView visualSubtitle = row.findViewById(R.id.visualSubtitle);

        // Get cover image (if no config file -> make new one with most recent file)
        String path = mediaList.get(pos);
        File cover = mediaManager.getCover(path);

        // Set properties of elements
        visualTitle.setText(path.substring(path.lastIndexOf("/") + 1));
        visualSubtitle.setText(String.format(Locale.getDefault(), "%d", mediaCountList.get(pos)));
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(cover)
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(visual);
        row.setId(pos);
        return (row);
    }

    public void addAll(ArrayList<String> mediaList, ArrayList<Integer> mediaCountList) {
        this.mediaList.clear();
        this.mediaList.addAll(mediaList);
        this.mediaCountList.clear();
        this.mediaCountList.addAll(mediaCountList);
        notifyDataSetChanged();
    }
}
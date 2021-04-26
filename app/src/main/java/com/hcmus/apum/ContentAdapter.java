package com.hcmus.apum;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import static com.hcmus.apum.MainActivity.debugEnabled;
import static com.hcmus.apum.MainActivity.mediaManager;

public class ContentAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> mediaList;

    public ContentAdapter(Context context, ArrayList<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return mediaList.size(); }
    @Override
    public Object getItem(int pos) { return mediaList.get(pos); }
    @Override
    public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ImageView img;
        int gridSize = context.getResources().getDimensionPixelOffset(R.dimen.gridview_size);
        // Use existing convertView in cache (if possible)
        if (convertView == null) {
            img = new ImageView(context);
            img.setLayoutParams(new GridView.LayoutParams(gridSize, gridSize));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setPadding(5, 5, 5, 5);
        } else {
            img = (ImageView) convertView;
        }

        // Generate thumbnails
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(new File(mediaList.get(pos)))
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(img);
        img.setId(pos);
        return img;
    }
}

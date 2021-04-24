package com.hcmus.apum;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    Context context;
    LayoutInflater inflater;
    ArrayList<String> mediaList;

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
    public View getView(int pos, View view, ViewGroup viewGroup) {
        // Get elements
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.layout_visual_listview, null);  // Preview popup
        ImageView visual = row.findViewById(R.id.visual);
        TextView visualTitle = row.findViewById(R.id.visualTitle);
        TextView visualSubtitle = row.findViewById(R.id.visualSubtitle);

        // Set properties of elements
        String path = mediaList.get(pos);
        visualTitle.setText(path.substring(path.lastIndexOf("/") + 1));
        visualSubtitle.setText(mediaManager.getModifiedTime(path));
        visualSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(new File(path))
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(visual);
        row.setId(pos);
        return(row);
    }
}

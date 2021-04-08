package com.hcmus.apum;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.mediaManager;
import static com.hcmus.apum.MainActivity.mediaPathList;

public class AlbumThumbnailAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;

    public AlbumThumbnailAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Number of entries in dataSet
    public int getCount() { return mediaPathList.size(); }

    // Get current item, its id
    public Object getItem(int pos) { return mediaPathList.get(pos); }
    public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get elements
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.layout_albums_listview, null);  // Preview popup
        TextView name = (TextView) row.findViewById(R.id.name);
        ImageView img = (ImageView) row.findViewById(R.id.icon);

        // Set properties of elements
        img.setImageBitmap(mediaManager.createThumbnail(mediaPathList.get(position)));
        row.setId(position);
        return(row);
    }
}

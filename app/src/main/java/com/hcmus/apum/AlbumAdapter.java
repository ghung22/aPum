package com.hcmus.apum;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.debugEnabled;
import static com.hcmus.apum.MainActivity.mediaManager;

public class AlbumAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> mediaList;
    private final ArrayList<Integer> mediaCount;

    public AlbumAdapter(Context context, ArrayList<String> mediaList, ArrayList<Integer> mediaCount) {
        this.context = context;
        this.mediaList = mediaList;
        this.mediaCount = mediaCount;
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
        // Get elements
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.layout_albums_listview, null);  // Preview popup
        TextView name = row.findViewById(R.id.name);
        TextView count = row.findViewById(R.id.count);
        ImageView img = row.findViewById(R.id.icon);

        // Set properties of elements
        String path = mediaList.get(pos);
        name.setText(path.substring(path.lastIndexOf("/") + 1));
        count.setText(String.format("%d", mediaCount.get(pos)));
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(new File(mediaList.get(pos)))
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(img);
        row.setId(pos);
        return(row);
    }
}

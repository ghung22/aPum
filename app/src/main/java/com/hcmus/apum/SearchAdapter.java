package com.hcmus.apum;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.debugEnabled;

public class SearchAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    ArrayList<String> mediaList;

    public SearchAdapter(Context context, ArrayList<String> mediaList) {
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
        View row = inflater.inflate(R.layout.layout_search_listview, null);  // Preview popup
        TextView result = row.findViewById(R.id.result);
        TextView location = row.findViewById(R.id.location);
        ImageView preview = row.findViewById(R.id.preview);

        // Set properties of elements
        String path = mediaList.get(pos);
        result.setText(path.substring(path.lastIndexOf("/") + 1));
        String pathDir = path.substring(0, path.lastIndexOf("/"));
        location.setText(pathDir.substring(pathDir.lastIndexOf("/") + 1));
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(new File(mediaList.get(pos)))
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(preview);
        row.setId(pos);
        return(row);
    }
}

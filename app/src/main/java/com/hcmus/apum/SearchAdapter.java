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
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.debugEnabled;
import static com.hcmus.apum.MainActivity.mediaManager;

public class SearchAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    ArrayList<String> mediaList;
    String scope;

    public SearchAdapter(Context context, ArrayList<String> mediaList, String scope) {
        this.context = context;
        this.mediaList = mediaList;
        this.scope = scope;
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
        ImageView preview = row.findViewById(R.id.visual);
        TextView result = row.findViewById(R.id.visualTitle);
        TextView location = row.findViewById(R.id.visualSubtitle);

        // Get cover image depending on scope
        String path = mediaList.get(pos);
        File cover = null;
        switch (scope) {
            case "overview":
                cover = new File(path);
                break;
            case "albums":
                cover = mediaManager.getCover(path);
                break;
        }

        // Set properties of elements
        result.setText(path.substring(path.lastIndexOf("/") + 1));
        String pathDir = path.substring(0, path.lastIndexOf("/"));
        location.setText(pathDir.substring(pathDir.lastIndexOf("/") + 1));
        location.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        Picasso picasso = Picasso.get();
        picasso.setLoggingEnabled(debugEnabled);
        picasso.load(cover)
                .fit()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .into(preview);
        row.setId(pos);
        return(row);
    }
}

package com.hcmus.apum;

import android.content.Context;
import android.database.SQLException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.mediaManager;

public class FavoriteAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> fav_images;
    DatabaseFavorites db_fav;
    public FavoriteAdapter(Context context) {
        this.context = context;
        //this.fav_images = mediaManager.getFavorites();
        //Init Database
        db_fav = new DatabaseFavorites(context);
        try {
            db_fav.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            db_fav.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        this.fav_images = db_fav.getAllFavorite();
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Number of entries in dataSet
    public int getCount() { return fav_images.size(); }

    // Get current item, its id
    public Object getItem(int pos) { return fav_images.get(pos); }
    public long getItemId(int pos) { return pos; }

    // Create a view for each thumbnail
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView img = null;
        // Use existing convertView in cache (if possible)
        if (convertView == null) {
            img = new ImageView(context);
            int gridSize = context.getResources().getDimensionPixelOffset(R.dimen.gridview_size);
            img.setLayoutParams(new GridView.LayoutParams(gridSize, gridSize));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setPadding(5, 5, 5, 5);
        } else {
            img = (ImageView) convertView;
        }

        // Generate thumbnails
        img.setImageBitmap(mediaManager.createThumbnail(fav_images.get(position)));
        img.setId(position);
        return img;
    }

}
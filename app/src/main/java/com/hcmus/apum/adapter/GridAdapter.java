package com.hcmus.apum.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.hcmus.apum.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.debugEnabled;

public class GridAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> mediaList;
    private final ArrayList<Rect> boundingBoxes;

    public GridAdapter(Context context, ArrayList<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
        this.boundingBoxes = null;
    }

    public GridAdapter(Context context, ArrayList<String> mediaList, ArrayList<Rect> boundingBoxes) {
        this.context = context;
        this.mediaList = mediaList;
        this.boundingBoxes = boundingBoxes;
    }

    public void add(String media) {
        mediaList.add(media);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<String> mediaList) {
        this.mediaList.clear();
        this.mediaList.addAll(mediaList);
        notifyDataSetChanged();
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
        if (boundingBoxes == null) {
            picasso.load(new File(mediaList.get(pos)))
                    .fit()
                    .config(Bitmap.Config.RGB_565)
                    .centerInside()
                    .placeholder(R.drawable.ic_image)
                    .into(img);
        } else {
            Rect box = boundingBoxes.get(pos);
            Integer left = box.left, top = box.top, right = box.right, bottom = box.bottom;
            picasso.load(new File(mediaList.get(pos)))
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            Bitmap result;
                            try {
                                result = Bitmap.createBitmap(source, Math.abs(left), top, right - left, bottom - top);
                            } catch (Exception e) {
                                result = Bitmap.createBitmap(
                                        source,
                                        Math.abs(left),
                                        Math.abs(top),
                                        Math.abs(right - left),
                                        Math.abs(bottom - top)
                                );
                            }
                            source.recycle();
                            return result;
                        }

                        @Override
                        public String key() {
                            return left + "," + top + "," + right + "," + bottom;
                        }
                    })
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.ic_image)
                    .into(img);
        }
        img.setId(pos);
        return img;
    }

}
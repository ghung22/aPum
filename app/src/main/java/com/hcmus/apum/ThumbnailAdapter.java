package com.hcmus.apum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ThumbnailAdapter extends BaseAdapter {
    private Context context;
    int[] thumbnails;
    LayoutInflater inflater;

    public ThumbnailAdapter(Context context, int[] images) {
        this.context = context;
        this.thumbnails = images;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Number of entries in dataSet
    public int getCount() { return thumbnails.length; }

    // Get current item, its id
    public Object getItem(int pos) { return thumbnails[pos]; }
    public long getItemId(int pos) { return pos; }

    // Create a view for each thumbnail
    public View getView(int pos, View convertView, ViewGroup parent) {
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

            // Generate thumbnails
            img.setImageResource(thumbnails[pos]);
//            img.setImageBitmap(getThumbnail(pos));
            img.setId(pos);
        }
        return img;
    }

    public Bitmap getThumbnail(int imgId) {
        File img = new File(Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + imgId).toString());
        BitmapFactory.Options bitmapOpt = new BitmapFactory.Options();
        bitmapOpt.inJustDecodeBounds = true;  // Get img size
        BitmapFactory.decodeFile(img.getAbsolutePath(), bitmapOpt);

        // find the best scaling factor for the desired dimensions
        int preferredW = 400, preferredH = 300;
        float wScale = (float) bitmapOpt.outWidth / preferredW,
                hScale = (float) bitmapOpt.outHeight / preferredH;
        float scale = Math.min(wScale, hScale);
        int sampleSize = 1;
        while (sampleSize < scale) {
            sampleSize *= 2;
        }
        bitmapOpt.inSampleSize = sampleSize;  // inSampleSize must be power of 2
        bitmapOpt.inJustDecodeBounds = false;  // Load the image

        // Load part of image to make thumbnail
        Bitmap thumbnail = BitmapFactory.decodeFile(img.getAbsolutePath(), bitmapOpt);

        // Save the thumbnail
        try {
            File thumbnailFile = null; // TODO: create file into thumbnail folder
            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Use the thumbnail on an ImageView or recycle it!
        return thumbnail;
    }
}
package com.hcmus.apum;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MediaManager {
    private ArrayList<String> images, albums, favorites;
    private ArrayList<Integer> albumCounts;
    DatabaseFavorites db;

    public void updateLocations(Context context) {
        ArrayList<String> images = new ArrayList<>(),
                albums = new ArrayList<>();
        ArrayList<Integer> albumCounts = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            String imagePath = cursor.getString(column_index_data),
                    albumPath = "";
            String[] folders = imagePath.split("/");
            for (int i = 0; i < folders.length - 1; ++i) {
                albumPath += folders[i];
                if (i < folders.length - 2) {
                    albumPath += "/";
                }
            }

            images.add(imagePath);
            if (!albums.contains(albumPath)) {
                albums.add(albumPath);
            }
        }

        for (String a: albums) {
            Integer count = 0;
            for (String i: images) {
                if (i.contains(a)) {
                    count++;
                }
            }
            albumCounts.add(count);
        }

        this.images = images;
        this.albums = albums;
        this.albumCounts = albumCounts;
    }

    public void updateFavoriteLocations(Context context) {
        ArrayList<String> listFavorites = new ArrayList<>();
        //listFavorites = db.getAllFavorite();
        favorites = listFavorites;
    }

    public void addFavorites(ArrayList<String> thumbs, int pos, DatabaseFavorites db){
        if(!favorites.contains(thumbs.get(pos))){
            favorites.add(thumbs.get(pos));
            db.addData(thumbs.get(pos));
        }else{
            favorites.remove(thumbs.get(pos));
            db.removeData(thumbs.get(pos));
        }
        //System.out.println("TEST 123 " + db.addData(thumbs.get(pos)));
        //db.addData(favorites.get(favorites.size()-1));
        //db.addData(i.get(pos));
    }

    public boolean checkFavorites(ArrayList<String> thumbs, int pos){
        boolean check = thumbs.contains(thumbs.get(pos));
        return check;
    }

    public ArrayList<String> getImages() { return images; }
    public ArrayList<String> getAlbums() { return  albums; }
    public ArrayList<String> getFavorites() { return favorites; }
    public ArrayList<Integer> getAlbumCounts() { return albumCounts; }

    public Bitmap createThumbnail(String path) {
        File img = new File(path);
        if (!img.exists()) {
            img = new File(Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + R.drawable.ic_image).toString());
        }
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

        // Use the thumbnail on an ImageView or recycle it!
        return thumbnail;
    }

    public ArrayList<String> search(String query, String scope) {
        // TODO: Search images, albums, favorites
        ArrayList<String> results = new ArrayList<>();

        return results;
    }
}

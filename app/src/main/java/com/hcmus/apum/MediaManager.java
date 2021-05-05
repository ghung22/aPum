package com.hcmus.apum;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.hcmus.apum.MainActivity.mediaManager;

public class MediaManager {
    private ArrayList<String> images, albums, faces, favorites;
    public final ArrayList<String>
            extImg = new ArrayList<>(
            Arrays.asList("gif", "png", "bmp", "jpg", "svg", "raw", "jpeg", "webp")
    );
    public final ArrayList<String> extVid = new ArrayList<>(
            Arrays.asList("mp4", "mov", "mkv", "wmv", "avi", "flv", "webm")
    );
    DatabaseFavorites db;

    public void updateLocations(Context context) {
        ArrayList<String> images = new ArrayList<>(),
                albums = new ArrayList<>();
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

        this.images = images;
        this.albums = albums;
    }

    public void updateFavoriteLocations(Context context) {
        ArrayList<String> listFavorites = new ArrayList<>();
        //listFavorites = db.getAllFavorite();
        favorites = listFavorites;
    }

    public void addFavorites(ArrayList<String> thumbs, int pos, DatabaseFavorites db) {
        if (!favorites.contains(thumbs.get(pos))) {
            favorites.add(thumbs.get(pos));
            db.addData(thumbs.get(pos));
        } else {
            favorites.remove(thumbs.get(pos));
            db.removeData(thumbs.get(pos));
        }
        //System.out.println("TEST 123 " + db.addData(thumbs.get(pos)));
        //db.addData(favorites.get(favorites.size()-1));
        //db.addData(i.get(pos));
    }

    public boolean isFavorite(String thumb) {
        boolean check = favorites.contains(thumb);
        return check;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public ArrayList<String> getAlbums() {
        return albums;
    }

    public ArrayList<String> getFavorites() {
        return favorites;
    }

    public ArrayList<Integer> getAlbumCounts(ArrayList<String> albums) {
        ArrayList<Integer> albumCounts = new ArrayList<>();
        for (String album : albums) {
            File dir = new File(album);
            if (!dir.isDirectory()) {
                continue;
            }
            albumCounts.add(dir.listFiles(getFileFilter("img")).length);
        }
        return albumCounts;
    }

    public FilenameFilter getFileFilter(String type) {
        FilenameFilter filter;
        switch (type) {
            case "img":
                filter = (dir, file) -> {
                    for (final String ext : extImg) {
                        if (file.endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                };
                break;
            case "vid":
                filter = (dir, file) -> {
                    for (final String ext : extVid) {
                        if (file.endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                };
                break;
            case "visual":
                filter = (dir, file) -> {
                    ArrayList<String> extVisual = extImg;
                    extVisual.addAll(extVid);
                    for (final String ext : extVisual) {
                        if (file.endsWith("." + ext)) {
                            return true;
                        }
                    }
                    return false;
                };
            default:
                filter = (dir, file) -> {
                    return true;
                };
                break;
        }
        return filter;
    }

    public File getLastModified(File[] list) {
        long modified = Long.MIN_VALUE;
        File file = null;
        for (File f : list) {
            if (f.lastModified() > modified) {
                file = f;
                modified = f.lastModified();
            }
        }
        return file;
    }

    public String getModifiedTime(String path, String format) {
        if (format.isEmpty()) {
            format = "dd-MM-uuuu HH:mm";
        }
        FileTime modTime = null;
        try {
            modTime = Files.getLastModifiedTime(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (format.equals("epoch")) {
            return String.valueOf(modTime.toInstant().getEpochSecond());
        }
        return DateTimeFormatter.ofPattern(format, Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())
                .format(modTime.toInstant());
    }

    public String getModifiedTime(String path) {
        return getModifiedTime(path, "");
    }

    public String getSize(String path) {
        File f = new File(path);
        if (!f.isFile()) {
            return "null";
        }
        float size = f.length();
        if (size < 1024f) {
            return size + "B";
        }
        size /= 1024;
        if (size < 1024f) {
            return size + "KB";
        }
        size /= 1024;
        if (size < 1024f) {
            return size + "MB";
        }
        size /= 1024f;
        return size + "GB";
    }

    public ArrayList<String> getLocation(Context context, ExifInterface exif) throws IOException {
        ArrayList<String> geo = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context);
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        double _lat = -1, _long = -1;

        // Analise latitude
        if (latitude != null) {
            String[] latCoordinate = latitude.split(","),
                    latDegree = latCoordinate[0].split("/"),
                    latMinute = latCoordinate[1].split("/"),
                    latSecond = latCoordinate[2].split("/");
            _lat = Double.parseDouble(latDegree[0]) / Double.parseDouble(latDegree[1]) +
                    Double.parseDouble(latMinute[0]) / Double.parseDouble(latMinute[1]) / 60 +
                    Double.parseDouble(latSecond[0]) / Double.parseDouble(latSecond[1]) / 3600;
        } else {
            latitude = "";
        }

        // Analise longitude
        if (longitude != null) {
            String[] longCoordinate = longitude.split(","),
                    longDegree = longCoordinate[0].split("/"),
                    longMinute = longCoordinate[1].split("/"),
                    longSecond = longCoordinate[2].split("/");
            _long = Double.parseDouble(longDegree[0]) / Double.parseDouble(longDegree[1]) +
                    Double.parseDouble(longMinute[0]) / Double.parseDouble(longMinute[1]) / 60 +
                    Double.parseDouble(longSecond[0]) / Double.parseDouble(longSecond[1]) / 3600;
        } else {
            longitude = "";
        }

        // Parse latitude and longitude
        if (!latitude.isEmpty()) {
            geo.add(geocoder.getFromLocation(_lat, _long, 1).get(0).getAddressLine(0));
            geo.add(String.valueOf(_lat));
            geo.add(String.valueOf(_long));
        } else {
            geo.add("");
            geo.add("");
            geo.add("");
        }
        return geo;
    }

    public File getCover(String albumPath) {
        // TODO: Cover image config file
        File dir = new File(albumPath);
        return getLastModified(
                dir.listFiles(getFileFilter("img"))
        );
    }

    public ArrayList<String> getAlbumContent(String albumPath) {
        ArrayList<String> container = new ArrayList<>();
        File dir = new File(albumPath);
        for (File f : dir.listFiles(getFileFilter("img"))) {
            container.add(f.getAbsolutePath());
        }
        return container;
    }

    public HashMap<String, String> getInfo(Context context, String path) {
        HashMap<String, String> info = new HashMap<>();

        // Check extension for file type
        String ext = path.substring(path.lastIndexOf('.') + 1);
        if (extImg.contains(ext)) {
            // Get data from image
            try {
                ExifInterface exif = new ExifInterface(path);
                // File attribute
                info.put("fileName", path.substring(path.lastIndexOf('/') + 1));
                info.put("fileSize", getSize(path));
                info.put("fileLocation", path.substring(0, path.lastIndexOf('/')));
                // Image attribute
                // TODO: Image size not showing
                info.put("imageSize", exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
                        + "x" +
                        exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
                info.put("description", exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                info.put("camera", exif.getAttribute(ExifInterface.TAG_MAKE));
                info.put("artist", exif.getAttribute(ExifInterface.TAG_ARTIST));
                ArrayList<String> geo = getLocation(context, exif);
                if (!geo.isEmpty()) {
                    info.put("imageLocation", geo.get(0));
                    info.put("imageLocationLat", geo.get(1));
                    info.put("imageLocationLong", geo.get(2));
                }
            } catch (IOException e) {
                info.put("err", "file not found");
                return info;
            }
        } else if (extVid.contains(ext)) {
            // Get data from video
            info.put("err", "video not supported yet");
        } else {
            info.put("err", "unsupported file type");
        }

        return info;
    }

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
        ArrayList<String> results = new ArrayList<>(),
                scopedList;
        switch (scope) {
            case "overview":
                scopedList = images;
                break;
            case "albums":
                scopedList = albums;
                break;
            case "faces":
                scopedList = faces;
                break;
            case "favorite":
                scopedList = favorites;
                break;
            default:
                return results;
        }
        query = query.toLowerCase();
        for (String i : scopedList) {
            String i_lower = i.toLowerCase();
            // Search by name
            if (i_lower.substring(i.lastIndexOf("/") + 1).contains(query)) {
                if (!results.contains(i)) {
                    results.add(i);
                }
            }

            // Search by album names
            if (scope.equals("overview")) {
                String dir = i_lower.substring(0, i.lastIndexOf("/"));
                if (dir.substring(dir.lastIndexOf("/") + 1).contains(query)) {
                    if (!results.contains(i)) {
                        results.add(i);
                    }
                }
            }

            /* Search by modified time
             * Supported formats are: 2021, 05-2021, Nov2021, Nov, November, November 2021,
             * 24-11-2021, 11-24-2021, 24 Nov 2021, Nov 24 2021, 24 November 2021, November 24 2021
             */
            String year = getModifiedTime(i, "uuuu"),
                    month = getModifiedTime(i, "MM"),
                    part_month = getModifiedTime(i, "MMM").toLowerCase(),
                    full_month = getModifiedTime(i, "MMMM").toLowerCase(),
                    day = getModifiedTime(i, "dd"),
                    q = query.replaceAll("\\s*-*/*", "").toLowerCase();
            if (q.equals(year) || q.equals(month + year) || q.equals(part_month + year) ||
                    q.equals(part_month) || q.equals(full_month) || q.equals(full_month + year) ||
                    q.equals(day + month + year) || q.equals(month + day + year) ||
                    q.equals(day + part_month + year) || q.equals(part_month + day + year) ||
                    q.equals(day + full_month + year) || q.equals(full_month + day + year)) {
                if (!results.contains(i)) {
                    results.add(i);
                }
            }

            // TODO: Search by geo-location

            // TODO: Search by tags

            // TODO: Search by color using AI (Optional)
        }

        return results;
    }

    public ArrayList<String> sort(@NonNull ArrayList<String> org, String type, boolean ascending) {
        ArrayList<String> sorted = new ArrayList<>();
        switch (type) {
            case "name":
                TreeMap<String, String> names = new TreeMap<>();
                for (String path : org) {
                    names.put(path.substring(path.lastIndexOf('/')), path);
                }
                for (Map.Entry<String, String> set : names.entrySet()) {
                    sorted.add(set.getValue());
                }
                break;
            case "date":
                TreeMap<Long, String> dates = new TreeMap<>();
                for (String path : org) {
                    dates.put(Long.parseLong(getModifiedTime(path, "epoch")), path);
                }
                for (Map.Entry<Long, String> set : dates.entrySet()) {
                    sorted.add(set.getValue());
                }
                break;
            default:
                sorted = org;
                break;
        }
        if (!ascending) {
            Collections.reverse(sorted);
        }

        return sorted;
    }

    public ArrayList<String> sort(ArrayList<String> org, String type) {
        return sort(org, type,true);
    }
}

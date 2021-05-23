package com.hcmus.apum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.core.content.FileProvider;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.hcmus.apum.component.LayoutDialog;
import com.hcmus.apum.fragment.FacesFragment;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.hcmus.apum.MainActivity.debugEnabled;

public class MediaManager {
    // Debugging
    private final String TAG = "MEDIA_MANAGER";

    // Data
    private ArrayList<String> media, albums, faces, favorites;
    private HashMap<String, ArrayList<String>> faceData = new HashMap<>();

    // Constant
    public final ArrayList<String>
            extImg = new ArrayList<>(
            Arrays.asList("gif", "png", "bmp", "jpg", "svg", "raw", "jpeg", "webp")
    );
    public final ArrayList<String> extVid = new ArrayList<>(
            Arrays.asList("mp4", "mov", "mkv", "wmv", "avi", "flv", "webm")
    );

    // Sort codes
    public final int
            SORT_DEFAULT = 0,
            SORT_BY_NAME = 10,
            SORT_BY_DATE = 20,
            SORT_ASCENDING = 0,
            SORT_DESCENDING = 1;

    // Global agent
    private Database database;
    private AsyncFacesUpdater faceUpdater;

    public void updateLocations(Context context) {
        ArrayList<String> images = new ArrayList<>(),
                albums = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            String imagePath = cursor.getString(column_index_data);
            StringBuilder albumPath = new StringBuilder();
            String[] folders = imagePath.split("/");
            for (int i = 0; i < folders.length - 1; ++i) {
                albumPath.append(folders[i]);
                if (i < folders.length - 2) {
                    albumPath.append("/");
                }
            }

            images.add(imagePath);
            if (!albums.contains(albumPath.toString())) {
                albums.add(albumPath.toString());
            }
        }

        this.media = images;
        this.albums = albums;
        cursor.close();
    }

    public void updateFavorite(Context context) {
        if (database == null) {
            database = new Database(context);
        }
        favorites = database.getFavorite();
    }

    public void updateFaces(Context context, FacesFragment fragment) {
        try {
            if (faceUpdater == null) {
                faces = new ArrayList<>();
                faceData = new HashMap<>();
                faceUpdater = new AsyncFacesUpdater(context, fragment);
                faceUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (faceUpdater.getStatus() != AsyncTask.Status.RUNNING) {
                faceUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            if (debugEnabled) {
                Log.e(TAG, "Update faces failed with message: " + (Strings.isEmptyOrWhitespace(e.getMessage()) ? "Unknown error" : e.getMessage()));
                Toast.makeText(context, "(!) Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean toggleFavorite(String media) {
        if (!favorites.contains(media)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("string", media);
            if (database.insert(map, Database.TABLE_FAVORITE)) {
                favorites.add(0, media);
                return true;
            }
        } else {
            if (database.delete(media, Database.TABLE_FAVORITE)) {
                favorites.remove(media);
                return true;
            }
        }
        return false;
    }

    public boolean isFavorite(String media) {
        return favorites.contains(media);
    }

    public ArrayList<String> getMedia() {
        return media;
    }

    public ArrayList<String> getFavorite() {
        return favorites;
    }

    public ArrayList<String> getAlbums() {
        return albums;
    }

    public ArrayList<String> getFaces() {
        return faces;
    }

    public HashMap<String, ArrayList<String>> getFaceData() {
        return faceData;
    }

    public ArrayList<Rect> getFaceRect(ArrayList<String> container) {
        // Convert Strings into Rect objects
        ArrayList<Rect> faceRect = new ArrayList<>();
        for (String con : container) {
            String[] sizesStr = con.split(",");
            Rect rect = new Rect(
                    Integer.parseInt(sizesStr[0]),
                    Integer.parseInt(sizesStr[1]),
                    Integer.parseInt(sizesStr[2]),
                    Integer.parseInt(sizesStr[3])
            );
            faceRect.add(rect);
        }
        return faceRect;
    }


    public Bitmap getCompressedBitmap(String path, int quality) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }

    public ArrayList<Integer> getAlbumCounts(ArrayList<String> albums) {
        ArrayList<Integer> albumCounts = new ArrayList<>();
        for (String album : albums) {
            File dir = new File(album);
            if (!dir.isDirectory()) {
                continue;
            }
            File[] list = dir.listFiles(getFileFilter("img"));
            if (list != null) {
                albumCounts.add(list.length);
            } else {
                albumCounts.add(-1);
            }
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
                break;
            default:
                filter = (dir, file) -> true;
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

        if (modTime != null) {
            if (format.equals("epoch")) {
                return String.valueOf(modTime.toInstant().getEpochSecond());
            }
            return DateTimeFormatter.ofPattern(format, Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault())
                    .format(modTime.toInstant());
        } else {
            Log.e(TAG, "getModifiedTime failed");
            return format;
        }
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

        // Convert lat and long to double
        for (String l : Arrays.asList(latitude, longitude)) {
            if (l != null) {
                String[] coordinate = l.split(","),
                        degree = coordinate[0].split("/"),
                        minute = coordinate[1].split("/"),
                        second = coordinate[2].split("/");
                double _l = Double.parseDouble(degree[0]) / Double.parseDouble(degree[1]) +
                        Double.parseDouble(minute[0]) / Double.parseDouble(minute[1]) / 60 +
                        Double.parseDouble(second[0]) / Double.parseDouble(second[1]) / 3600;
                geo.add(String.valueOf(_l));
            } else {
                geo.add("");
                geo.add("");
                geo.add("");
            }
        }

        // Return found address, lat and long
        double _lat = Double.parseDouble(geo.get(0)), _long = Double.parseDouble(geo.get(1));
        geo.add(0, geocoder.getFromLocation(_lat, _long, 1).get(0).getAddressLine(0));
        return geo;
    }

    public File getCover(String albumPath) {
        // TODO: Cover image config file
        File dir = new File(albumPath);
        return getLastModified(
                Objects.requireNonNull(dir.listFiles(getFileFilter("img")))
        );
    }

    public ArrayList<String> getAlbumContent(String albumPath) {
        ArrayList<String> container = new ArrayList<>();
        File dir = new File(albumPath);
        for (File f : Objects.requireNonNull(dir.listFiles(getFileFilter("img")))) {
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

    public ArrayList<String> search(String query, String scope) {
        ArrayList<String> results = new ArrayList<>(),
                scopedList;
        switch (scope) {
            case "overview":
                scopedList = media;
                break;
            case "albums":
                scopedList = albums;
                break;
            case "faces":
                // scopedList = faces;
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

    public ArrayList<String> sort(ArrayList<String> org, int sortMethod, int sortOrder) {
        // Init data
        if (org == null) {
            return new ArrayList<>();
        }
        ArrayList<String> sorted = new ArrayList<>();
        int SORT_CODE = sortMethod + sortOrder;

        // Sort method
        if (SORT_CODE == SORT_DEFAULT) {
            return org;
        }
        if (SORT_CODE / 10 == SORT_BY_NAME) {
            TreeMap<String, String> names = new TreeMap<>();
            for (String path : org) {
                names.put(path.substring(path.lastIndexOf('/')), path);
            }
            for (Map.Entry<String, String> set : names.entrySet()) {
                sorted.add(set.getValue());
            }
        } else if (SORT_CODE / 10 == SORT_BY_DATE) {
            TreeMap<Long, String> dates = new TreeMap<>();
            for (String path : org) {
                dates.put(Long.parseLong(getModifiedTime(path, "epoch")), path);
            }
            for (Map.Entry<Long, String> set : dates.entrySet()) {
                sorted.add(set.getValue());
            }
        } else {
            sorted = org;
        }

        // Sort order
        if (SORT_CODE % 10 == SORT_DESCENDING) {
            Collections.reverse(sorted);
        }

        return sorted;
    }

    public ArrayList<String> sort(ArrayList<String> org, int sortCode) {
        return sort(org, sortCode, 0);
    }

    public void sortUI(Context context, String caller, ArrayList<String> mediaList) {
        // INIT ELEMENTS
        LayoutDialog dialog = new LayoutDialog(context, R.layout.layout_sort_dialog);
        RadioGroup sort_radio_group_method = dialog.findViewById(R.id.sort_radio_group_method),
                sort_radio_group_order = dialog.findViewById(R.id.sort_radio_group_order);
        LinearLayout sort_err_row = dialog.findViewById(R.id.sort_err_row);
        Button sort_cancel_btn = dialog.findViewById(R.id.sort_cancel_btn),
                sort_sort_btn = dialog.findViewById(R.id.sort_sort_btn);

        // APPLY DATA
        final int[] sortMethod = {SORT_BY_NAME},
                sortOrder = {SORT_ASCENDING};
        sort_err_row.setVisibility(View.GONE);

        // INIT CONTROLS
        sort_radio_group_method.setOnCheckedChangeListener((radioGroup, radioId) -> {
            if (radioId == R.id.sort_radio_by_name) {
                sortMethod[0] = SORT_BY_NAME;
            } else if (radioId == R.id.sort_radio_by_date) {
                sortMethod[0] = SORT_BY_DATE;
            }
        });
        sort_radio_group_order.setOnCheckedChangeListener(((radioGroup, radioId) -> {
            if (radioId == R.id.sort_radio_ascending) {
                sortOrder[0] = SORT_ASCENDING;
            } else if (radioId == R.id.sort_radio_descending) {
                sortOrder[0] = SORT_DESCENDING;
            }
        }));
        sort_cancel_btn.setOnClickListener(view -> dialog.dismiss());
        sort_sort_btn.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("caller", caller);
            bundle.putString("action", "sort");
            bundle.putInt("sortCode", sortMethod[0] + sortOrder[0]);
            bundle.putStringArrayList("mediaList", sort(mediaList, sortMethod[0], sortOrder[0]));
            ((MainActivity) context).fragToMain(caller, bundle);
            dialog.dismiss();
        });

        dialog.show();
    }

    public boolean copy(String source, String destination) {
        destination += "/" + source.substring(source.lastIndexOf("/") + 1);
        // Alter destination file name if file exists
        File temp = new File(destination);
        if (temp.exists()) {
            int i = 0;
            do {
                String path = destination.substring(0, destination.lastIndexOf('/')),
                        file = destination.substring(destination.lastIndexOf('/') + 1);
                String[] parts = file.split("\\.");
                temp = new File(path + parts[0] + i + "." + parts[1]);
            } while (temp.exists());
            destination += Integer.toString(i);
        }

        // Copy bytes to new file
        try {
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(destination);

            FileChannel inChannel = in.getChannel(),
                    outChannel = out.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "Copy failed with message: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void refresh(Context context, String path) {
        File file = new File(path);
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()},
                null, null);
    }

    public boolean copy(Context context, String source, String destination) {
        boolean copy = copy(source, destination);
        refresh(context, destination);
        updateLocations(context);
        return copy;
    }

    public boolean delete(Context context, String path) {
        File file = new File(path);
        if (!file.delete()) {
            return false;
        } else {
            refresh(context, path);
            updateLocations(context);
        }
        return true;
    }

    public boolean move(Context context, String source, String destination) {
        boolean result = copy(source, destination) && delete(context, source);
        updateLocations(context);
        return result;
    }

    public void share(Context context, String path) {
        Intent mediaShare = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(
                context,
                "com.hcmus.apum.provider",
                new File(path));
        mediaShare.setType("image/*");
        mediaShare.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(mediaShare);
    }

    @SuppressLint("StaticFieldLeak")
    public class AsyncFacesUpdater extends AsyncTask<String, String, String> {
        // GUI controls
        private final Context context;
        private final FacesFragment fragment;
        private LayoutDialog dialog;
        private LinearLayout generate_err_row;
        private TextView generate_progress, generate_progress_info, generate_err;
        private ProgressBar generate_progress_bar;
        private Button generate_close_btn;

        // Data
        private final int maxProgress;
        private FaceDetector detector = null;

        public AsyncFacesUpdater(Context context, FacesFragment fragment) {
            super();
            this.context = context;
            this.fragment = fragment;
            maxProgress = media.size();
        }

        @Override
        protected void onPreExecute() {
            // INIT ELEMENTS
            dialog = new LayoutDialog(context, R.layout.layout_faces_generate_dialog);
            generate_err_row = dialog.findViewById(R.id.generate_err_row);
            generate_progress = dialog.findViewById(R.id.generate_progress);
            generate_progress_info = dialog.findViewById(R.id.generate_progress_info);
            generate_err = dialog.findViewById(R.id.generate_err);
            generate_progress_bar = dialog.findViewById(R.id.generate_progress_bar);
            generate_close_btn = dialog.findViewById(R.id.generate_close_btn);

            // APPLY DATA
            generate_progress_bar.setProgress(0);
            generate_progress_bar.setMax(maxProgress);
            generate_progress_info.setText("");
            generate_err_row.setVisibility(View.GONE);
            generate_close_btn.setOnClickListener(view -> dialog.dismiss());

            // PREPARE PROCESSING DATA
            if (detector == null) {
                FaceDetectorOptions options =
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                                .build();
                detector = FaceDetection.getClient(options);
            }
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                publishProgress("Loading images…");
                int i = 0;
                for (String path : media) {
                    publishProgress(path);
                    ArrayList<String> recs = new ArrayList<>();

                    InputImage img = InputImage.fromBitmap(getCompressedBitmap(path, 20), 0);
                    Task<List<Face>> resultTask =
                            detector.process(img)
                                    .addOnSuccessListener(result -> {
                                        if (!result.isEmpty()) {
                                            for (Face face : result) {
                                                Rect rect = face.getBoundingBox();
                                                recs.add(rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
                                            }
                                        }

                                        if (!recs.isEmpty()) {
                                            faces.add(path);
                                            faceData.put(path, recs);
                                            publishProgress(path, "done");
                                        }
                                    });
                    generate_progress_bar.setProgress(++i);
                    while (!resultTask.isComplete()) {

                    }
                }
                // Fake finishing step
                publishProgress("Finishing up…", "top");
                Random r = new Random();
                r.setSeed(maxProgress);
                Thread.sleep(1000 + r.nextInt(2000));
            } catch (Throwable e) {
                Log.e(TAG, "AsyncFacesUpdater encountered an error: " + (Strings.isEmptyOrWhitespace(e.getMessage()) ? "Unknown error" : e.getMessage()));
                return e.getMessage();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if (text.length > 0) {
                if (text.length < 2) {
                    generate_progress_info.setText(text[0]);
                } else {
                    if (text[1].equals("done")) {
                        // Add image path with faces into FacesFragment's mediaList
                        fragment.addMediaFace(text[0], faceData.get(text[0]));
                    } else if (text[1].equals("top")) {
                        generate_progress.setText(text[0]);
                    }
                }
            } else {
                generate_progress_info.setText("…");
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) {
                dialog.dismiss();
                Toast.makeText(context, R.string.info_faces_processing_completed, Toast.LENGTH_SHORT).show();
            } else {
                generate_err.setText(result);
                generate_progress_info.setVisibility(View.GONE);
                generate_err_row.setVisibility(View.VISIBLE);
            }
        }
    }
}
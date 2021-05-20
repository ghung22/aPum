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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
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
    // Data
    private ArrayList<String> images, albums, faces, favorites;
    private HashMap<String, ArrayList<String>> faceData = new HashMap<>();

    // Constant
    public final ArrayList<String>
            extImg = new ArrayList<>(
            Arrays.asList("gif", "png", "bmp", "jpg", "svg", "raw", "jpeg", "webp")
    );
    public final ArrayList<String> extVid = new ArrayList<>(
            Arrays.asList("mp4", "mov", "mkv", "wmv", "avi", "flv", "webm")
    );

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
                Log.e("FACES", Strings.isEmptyOrWhitespace(e.getMessage()) ? "Unknown error" : e.getMessage());
                Toast.makeText(context, "(!) Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void toggleFavorite(ArrayList<String> thumbs, int pos) {
        if (!favorites.contains(thumbs.get(pos))) {
            HashMap<String, String> map = new HashMap<>();
            map.put("String", thumbs.get(pos));
            database.insert(map, Database.TABLE_FAVORITE);
            favorites.add(thumbs.get(pos));
        } else {
            database.delete(thumbs.get(pos), Database.TABLE_FAVORITE);
            favorites.remove(thumbs.get(pos));
        }
    }

    public boolean isFavorite(String thumb) {
        return favorites.contains(thumb);
    }

    public ArrayList<String> getImages() {
        return images;
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
                scopedList = images;
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

    public ArrayList<String> sort(ArrayList<String> org, String type, boolean ascending) {
        if (org == null) {
            return new ArrayList<>();
        }
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

    public void sortUI(Context context, String caller, ArrayList<String> mediaList) {
        // INIT ELEMENTS
        LayoutDialog dialog = new LayoutDialog(context, R.layout.layout_sort_dialog);
        RadioGroup sort_radio_group_method = dialog.findViewById(R.id.sort_radio_group_method),
                sort_radio_group_order = dialog.findViewById(R.id.sort_radio_group_order);
        RadioButton sort_radio_by_name = dialog.findViewById(R.id.sort_radio_by_name),
                sort_radio_by_date = dialog.findViewById(R.id.sort_radio_by_date),
                sort_radio_ascending = dialog.findViewById(R.id.sort_radio_ascending),
                sort_radio_descending = dialog.findViewById(R.id.sort_radio_descending);
        LinearLayout sort_err_row = dialog.findViewById(R.id.sort_err_row);
        TextView sort_err = dialog.findViewById(R.id.sort_err);
        Button sort_cancel_btn = dialog.findViewById(R.id.sort_cancel_btn),
                sort_sort_btn = dialog.findViewById(R.id.sort_sort_btn);

        // APPLY DATA
        String[] method = { "name" };
        boolean[] ascending = { true };
        sort_err_row.setVisibility(View.GONE);

        // INIT CONTROLS
        sort_radio_group_method.setOnCheckedChangeListener((radioGroup, radioId) -> {
            if (radioId == R.id.sort_radio_by_name) {
                method[0] = "name";
            } else if (radioId == R.id.sort_radio_by_date) {
                method[0] = "date";
            }
        });
        sort_radio_group_order.setOnCheckedChangeListener(((radioGroup, radioId) -> {
            if (radioId == R.id.sort_radio_ascending) {
                ascending[0] = true;
            } else if (radioId == R.id.sort_radio_descending) {
                ascending[0] = false;
            }
        }));
        sort_cancel_btn.setOnClickListener(view -> dialog.dismiss());
        sort_sort_btn.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("caller", caller);
            bundle.putString("action", "sort");
            bundle.putStringArrayList("mediaList", sort(mediaList, method[0], ascending[0]));
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
            Log.e("COPY", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean copy(Context context, String source, String destination) {
        boolean copy = copy(source, destination);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(destination))));
        updateLocations(context);
        return copy;
    }

    public boolean delete(Context context, String path) {
        File file = new File(path);
        if (!file.delete()) {
            return false;
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
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
        private MenuItem regenerate;

        // Data
        private final int maxProgress;
        private FaceDetector detector = null;

        public AsyncFacesUpdater(Context context, FacesFragment fragment) {
            super();
            this.context = context;
            this.fragment = fragment;
            maxProgress = images.size();
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
            regenerate = fragment.getMenu().findItem(R.id.action_regenerate);

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
                for (String path : images) {
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
                                    })
                                    .addOnFailureListener(Throwable::getMessage);
                    generate_progress_bar.setProgress(++i);
                    while (!resultTask.isComplete()) {
                        // Wait till image is processed
                    }
                }
                // Fake finishing step
                publishProgress("Finishing up…");
                Random r = new Random();
                r.setSeed(maxProgress);
                Thread.sleep(1000 + r.nextInt(2000));
            } catch (Exception e) {
                if (debugEnabled) {
                    Log.e("FACES", Strings.isEmptyOrWhitespace(e.getMessage()) ? "Unknown error" : e.getMessage());
                }
                return e.getMessage();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if (text.length > 0) {
                generate_progress_info.setText(text[0]);
                if (text.length == 2) {
                    if (text[1].equals("done")) {
                        // Add image path with faces into FacesFragment's mediaList
                        fragment.addMediaFace(text[0], faceData.get(text[0]));
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
                regenerate.getActionView().getAnimation().cancel();
                regenerate.getActionView().getAnimation().reset();
                Toast.makeText(context, R.string.info_faces_processing_completed, Toast.LENGTH_SHORT).show();
            } else {
                generate_err.setText(result);
                generate_progress_info.setVisibility(View.GONE);
                generate_err_row.setVisibility(View.VISIBLE);
            }
        }
    }
}
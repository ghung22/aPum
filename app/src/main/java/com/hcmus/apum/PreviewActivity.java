package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static com.hcmus.apum.MainActivity.PREVIEW_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class PreviewActivity extends AppCompatActivity {

    // GUI controls
    private Toolbar toolbar;
    private BottomNavigationView bottomToolbar;

    // Elements
    private ViewPager imgPreview;
    private PreviewAdapter adapter;
    private LayoutDialog dialog;

    // Data
    ArrayList<String> mediaList;
    int pos;
    double latitude, longitude;

    //DB
    private DatabaseFavorites db_fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Get values from bundle
        Intent mainPreview = getIntent();
        Bundle bundle = mainPreview.getExtras();
        mediaList = bundle.getStringArrayList("thumbnails");
        pos = bundle.getInt("position");

        // Init preview layout
        adapter = new PreviewAdapter(this, mediaList);
        imgPreview = findViewById(R.id.img_preview);
        imgPreview.setAdapter(adapter);
        imgPreview.setCurrentItem(pos);
        setScroller(2f);
        imgPreview.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private float lastOffset = 0f;

            // Snap to page based on how much user have scrolled
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                if (offset < lastOffset && offset < .9f) {
                    imgPreview.setCurrentItem(position);
                } else if (offset > lastOffset && offset > .1f) {
                    imgPreview.setCurrentItem(position + 1);
                }
                lastOffset = offset;
            }

            // Update favorite status of current image
            @Override
            public void onPageSelected(int position) {
                Menu menu = bottomToolbar.getMenu();
                MenuItem fav = menu.findItem(R.id.action_favorite);
                if (mediaManager.isFavorite(mediaList.get(pos))) {
                    fav.setIcon(R.drawable.ic_fav);
                } else {
                    fav.setIcon(R.drawable.ic_fav_outline);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        //Database
        db_fav = new DatabaseFavorites(this);
        try {
            db_fav.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        db_fav.openDataBase();

        // Init actionbar buttons
        toolbar = findViewById(R.id.menu_preview);
        toolbar.inflateMenu(R.menu.menu_preview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String path = mediaList.get(pos);
            actionBar.setTitle(path.substring(path.lastIndexOf('/') + 1));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        bottomToolbar = findViewById(R.id.bottomBar_preview);
        bottomToolbar.setOnNavigationItemSelectedListener(item -> bottomToolbarAction((String) item.getTitle()));

        // Set values to return
        Intent previewMain = new Intent();
        Bundle returnBundle = new Bundle();
        returnBundle.putString("caller", bundle.getString("caller"));
        previewMain.putExtras(returnBundle);
        setResult(Activity.RESULT_OK, previewMain);
    }

    private void initInfoDialog() {
        // INIT ELEMENTS
        dialog = new LayoutDialog(this, R.layout.layout_preview_info_dialog);
        LinearLayout info_err_row = dialog.findViewById(R.id.info_err_row),
                info_fileName_row = dialog.findViewById(R.id.info_fileName_row),
                info_fileLocation_row = dialog.findViewById(R.id.info_fileLocation_row),
                info_description_row = dialog.findViewById(R.id.info_description_row),
                info_imageSize_row = dialog.findViewById(R.id.info_imageSize_row),
                info_camera_row = dialog.findViewById(R.id.info_camera_row),
                info_map_row = dialog.findViewById(R.id.info_map_row);
        ImageView info_fileName_img = dialog.findViewById(R.id.info_fileName_img);
        TextView info_err = dialog.findViewById(R.id.info_err),
                info_fileName = dialog.findViewById(R.id.info_fileName),
                info_fileLocation = dialog.findViewById(R.id.info_fileLocation),
                info_description = dialog.findViewById(R.id.info_description),
                info_imageSize = dialog.findViewById(R.id.info_imageSize),
                info_camera = dialog.findViewById(R.id.info_camera),
                info_map = dialog.findViewById(R.id.info_map);
        MapView info_map_view = dialog.findViewById(R.id.info_map_view);
        Button info_edit_btn = dialog.findViewById(R.id.info_edit_btn),
                info_close_btn = dialog.findViewById(R.id.info_close_btn);

        // APPLY DATA
        // Get data and check if occurred errors
        HashMap<String, String> data = mediaManager.getInfo(this, mediaList.get(pos));
        if (data.containsKey("err")) {
            info_fileName_row.setVisibility(View.GONE);
            info_fileLocation_row.setVisibility(View.GONE);
            info_description_row.setVisibility(View.GONE);
            info_imageSize_row.setVisibility(View.GONE);
            info_camera_row.setVisibility(View.GONE);
            info_map_row.setVisibility(View.GONE);
            info_err.setText(data.get("err"));
            dialog.show();
            return;
        } else {
            info_err_row.setVisibility(View.GONE);
        }
        String fileName = data.get("fileName"), fileLocation = data.get("fileLocation"),
                fileSize = data.get("fileSize"), description = data.get("description"),
                imageSize = data.get("imageSize"), camera = data.get("camera"),
                artist = data.get("artist"), imageLocation = data.get("imageLocation"),
                imageLocationLat = data.get("imageLocationLat"), imageLocationLong = data.get("imageLocationLong");
        // File attribute
        info_fileName.setText(HtmlCompat.fromHtml(fileName + " <i>(" + fileSize + ")</i>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        info_fileLocation.setText(fileLocation);
        // Media attribute
        if (!TextUtils.isEmpty(description)) {
            info_description.setText(description);
        } else {
            info_description_row.setVisibility(View.GONE);
        }
        info_imageSize.setText(imageSize);
        if (!TextUtils.isEmpty(camera) && !TextUtils.isEmpty(artist)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken with <b>" + camera + "</b> by <b>" + artist + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (!TextUtils.isEmpty(camera)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken with <b>" + camera + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (!TextUtils.isEmpty(artist)) {
            info_camera.setText(HtmlCompat.fromHtml("Taken by <b>" + artist + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            info_camera_row.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(imageLocationLat)) {
            latitude = Double.parseDouble(imageLocationLat);
            longitude = Double.parseDouble(imageLocationLong);
            if (!TextUtils.isEmpty(imageLocation)) {
                info_map.setText(imageLocation);
            } else {
                info_map.setText(getResources().getString(R.string.info_unknown_location));
            }
            MapsInitializer.initialize(this);
            info_map_view.onCreate(dialog.onSaveInstanceState());
            info_map_view.onResume();
            info_map_view.getMapAsync(googleMap -> {
                LatLng location = new LatLng(latitude, longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
            });
        } else {
            info_map_row.setVisibility(View.GONE);
        }

        // INIT CONTROLS
        info_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: View edit dialog
            }
        });
        info_close_btn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Menu menu = toolbar.getMenu();
        MenuItem fav = menu.findItem(R.id.action_favorite);
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            // Top toolbar
            case R.id.action_info:
                initInfoDialog();
                break;
            case R.id.action_wallpaper:
                break;

            // Bottom toolbar
            case R.id.action_favorite:
                mediaManager.addFavorites(mediaList, pos, db_fav);
                if(mediaManager.isFavorite(mediaList.get(pos))) {
                    fav.setIcon(R.drawable.ic_fav);
                    Toast.makeText(this, "Added to Favorite", Toast.LENGTH_LONG).show();
                } else {
                    fav.setIcon(R.drawable.ic_fav_outline);
                    Toast.makeText(this, "Removed from Favorite", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_edit:
                break;
            case R.id.action_share:
                break;
            case R.id.action_delete:
                deleteImg(mediaList.get(pos));
                break;
            default:
                Toast.makeText(this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private boolean bottomToolbarAction(String title) {
        if (title.equals(getResources().getString(R.string.fragment_favorite))) {

        } else if (title.equals(getResources().getString(R.string.action_edit))) {

        } else if (title.equals(getResources().getString(R.string.action_share))) {

        } else if (title.equals(getResources().getString(R.string.action_delete))) {

        } else {
            Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void deleteImg(String path_img){
        File f_del = new File(path_img);
        if(f_del.exists()){
            if(f_del.delete()){
                Log.e("Delete", "file Deleted :" + path_img);
                callBroadCast();
            }else{
                Log.e("Delete", "file not Deleted :" + path_img);
            }
        }
    }

    public void callBroadCast() {
        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage", "Scanned " + path + ":");
                Log.e("ExternalStorage", "-> uri=" + uri);
            }
        });
    }

    // Set custom scroll speed for ViewPager
    private void setScroller(float rate) {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            FixedSpeedScroller newScroller = new FixedSpeedScroller(this, (Interpolator) interpolator.get(null));
            newScroller.setScrollRate(rate);
            scroller.set(imgPreview, newScroller);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(PREVIEW_REQUEST_CODE);
    }
}

class FixedSpeedScroller extends Scroller {
    private double scrollRate = 1;

    public FixedSpeedScroller(Context context) {
        super(context);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollRate(double scrollRate) {
        this.scrollRate = scrollRate;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, (int) (duration * scrollRate));
    }
}
package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.PREVIEW_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class PreviewActivity extends AppCompatActivity {

    // GUI controls
    private Toolbar toolbar;
    private BottomNavigationView bottomToolbar;

    // Elements
    private ViewPager imgPreview;
    private PreviewAdapter adapter;

    // Bundle data
    ArrayList<String> thumbnails;
    int pos;

    //DB
    private DatabaseFavorites db_fav;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Get values from bundle
        Intent mainPreview = getIntent();
        Bundle bundle = mainPreview.getExtras();
        thumbnails = bundle.getStringArrayList("thumbnails");
        pos = bundle.getInt("position");
        File imgFile = new File(thumbnails.get(pos));

        // Init preview layout
        adapter = new PreviewAdapter(this, thumbnails);
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
                if (mediaManager.isFavorite(thumbnails.get(pos))) {
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
            actionBar.setTitle(imgFile.getName());
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
                Toast.makeText(this, pos, Toast.LENGTH_LONG).show();
                //Toast.makeText(getContext(),img.toString(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_wallpaper:
                break;

            // Bottom toolbar
            case R.id.action_favorite:
                mediaManager.addFavorites(thumbnails, pos, db_fav);
                if(mediaManager.isFavorite(thumbnails.get(pos))) {
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
                deleteImg(thumbnails.get(pos));
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
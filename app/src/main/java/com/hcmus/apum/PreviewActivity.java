package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity {

    // GUI controls
    Toolbar toolbar;
    AppBarLayout appbar;
    // Elements
    ImageView imgPreview;
    public ArrayList<String> images_fav;
    ArrayList<String> thumbnails;
    int pos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Init preview layout
        setContentView(R.layout.activity_preview);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        // Get values from bundle
        Intent mainPreview = getIntent();
        Bundle bundle = mainPreview.getExtras();
        String[] items = bundle.getStringArray("items");
        thumbnails = bundle.getStringArrayList("thumbnails");
        pos = bundle.getInt("position");
        File imgFile = new File(thumbnails.get(pos));
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgPreview.setImageBitmap(myBitmap);

        } else {

        }
//        imgPreview.setImageResource(thumbnails.get(pos));
        // Init actionbar buttons
        toolbar = (Toolbar) findViewById(R.id.menu_preview);
        toolbar.inflateMenu(R.menu.menu_preview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(imgFile.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
            Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_favorite:
                break;
            case R.id.action_info:
                images_fav.add(thumbnails.get(pos));
                Toast.makeText(this, "oke", Toast.LENGTH_LONG).show();
                //Toast.makeText(getContext(),img.toString(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_wallpaper:
                break;
        }
        return true;
    }
}
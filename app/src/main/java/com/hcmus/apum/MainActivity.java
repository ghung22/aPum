package com.hcmus.apum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // GUI controls
    GridView grid;
    Toolbar topBar, bottomBar;

    // For use/save state values
    Bundle savedInstanceState;

    // Test values
    String[] items = {"Ant","Baby","Clown", "Duck", "Elephant", "Family", "Good", "Happy", "Igloo",
            "Jumping", "King", "Love", "Mother", "Napkin", "Orange", "Pillow"};
    int[] images = {R.drawable.ant, R.drawable.baby, R.drawable.clown, R.drawable.duck,
            R.drawable.elephant, R.drawable.family, R.drawable.good, R.drawable.happy,
            R.drawable.igloo, R.drawable.jumping, R.drawable.king, R.drawable.love,
            R.drawable.mother, R.drawable.napkin, R.drawable.orange, R.drawable.pillow};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        // Init controls
        grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new ThumbnailAdapter(this, images));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showPreview(i);
            }
        });

        // Init actionbar buttons
        topBar = (Toolbar) findViewById(R.id.menu_main_top);
        topBar.inflateMenu(R.menu.menu_main_top);
        topBar = (Toolbar) findViewById(R.id.menu_main_bottom);
        topBar.inflateMenu(R.menu.menu_main_bottom);
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this, Preview.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray("items", items);
        bundle.putIntArray("thumbnails", images);
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, 97);
//        finish();
    }
}
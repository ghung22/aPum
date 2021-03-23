package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

public class PreviewActivity extends AppCompatActivity {

    // GUI controls
    Toolbar toolbar;

    // Elements
    ImageView imgPreview;

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
        int[] thumbnails = bundle.getIntArray("thumbnails");
        int pos = bundle.getInt("position");
        imgPreview.setImageResource(thumbnails[pos]);

        // Init actionbar buttons
        toolbar = (Toolbar) findViewById(R.id.menu_preview);
        toolbar.inflateMenu(R.menu.menu_preview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(items[pos]);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
package com.hcmus.apum.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.CONTENT_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class ContentActivity extends AppCompatActivity {
    private final Context context = ContentActivity.this;

    // GUI Controls
    private Toolbar toolbar;
    private GridView content;
    private GridAdapter adapter;

    // Content
    private String caller;
    private String host;
    private ArrayList<String> container;

    // Caller-dependant data
    private ArrayList<Rect> boundingBoxes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Get values from bundle
        Intent mainContent = getIntent();
        Bundle bundle = mainContent.getExtras();
        caller = bundle.getString("caller");
        host = bundle.getString("host");
        container = bundle.getStringArrayList("container");
        if (caller.equals("albums")) {
            container = mediaManager.sort(container, mediaManager.SORT_BY_DATE, mediaManager.SORT_DESCENDING);
        } else if (caller.equals("faces")) {
            // Convert Strings into Rect objects
            boundingBoxes = mediaManager.getFaceRect(container);
            // Create a list of image path (host) duplicates
            container.clear();
            for (int i = 0; i < boundingBoxes.size(); ++i) {
                container.add(host);
            }
            // Trim file path to file name for toolbar title
            host = host.substring(host.lastIndexOf("/") + 1);
        }

        // Update controls
        toolbar = findViewById(R.id.menu_content);
        toolbar.inflateMenu(R.menu.menu_overview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(host);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        adapter = new GridAdapter(context, container, boundingBoxes);
        content = findViewById(R.id.content);
        content.setEmptyView(findViewById(R.id.no_media));
        content.setAdapter(adapter);
        content.setOnItemClickListener((adapterView, view, i, l) -> showPreview(i));

        // Set values to return
        Intent contentMain = new Intent();
        Bundle returnBundle = new Bundle();
        returnBundle.putString("caller", caller);
        contentMain.putExtras(returnBundle);
        setResult(Activity.RESULT_OK, contentMain);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void showPreview(int pos) {
        Intent contentPreview = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("thumbnails", container);
        bundle.putInt("position", pos);
        contentPreview.putExtras(bundle);
        startActivityForResult(contentPreview, 97);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(CONTENT_REQUEST_CODE);
    }
}
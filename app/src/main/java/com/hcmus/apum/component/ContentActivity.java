package com.hcmus.apum.component;

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
import com.hcmus.apum.tool.ActivityManager;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.*;

public class ContentActivity extends AppCompatActivity {
    private final Context context = ContentActivity.this;

    private ArrayList<String> container;

    // Caller-dependant data
    private ArrayList<Rect> boundingBoxes = null;

    // Activity switching
    protected ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Get values from bundle
        Intent mainContent = getIntent();
        Bundle bundle = mainContent.getExtras();
        String caller = bundle.getString("caller");
        String host = bundle.getString("host");
        container = bundle.getStringArrayList("container");
        if (caller.equals(fragNames.get(1))) {
            container = mediaManager.sort(container, mediaManager.SORT_BY_DATE, mediaManager.SORT_DESCENDING);
        } else if (caller.equals(fragNames.get(2))) {
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

        // Activity handler
        activityManager = new ActivityManager(this, "content");
        activityManager.setResult(bundle.getString("caller"));

        // Update controls
        Toolbar toolbar = findViewById(R.id.menu_content);
        toolbar.inflateMenu(R.menu.menu_overview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(host);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        GridAdapter adapter = new GridAdapter(context, container, boundingBoxes);
        GridView content = findViewById(R.id.content);
        content.setEmptyView(findViewById(R.id.no_media));
        content.setAdapter(adapter);
        content.setOnItemClickListener(
                (adapterView, view, i, l) -> activityManager.showPreview(container, i)
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(CONTENT_REQUEST_CODE);
    }
}
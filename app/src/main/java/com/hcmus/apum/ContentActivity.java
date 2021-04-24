package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ContentActivity extends AppCompatActivity {
    private Context context = ContentActivity.this;

    // GUI Controls
    private Toolbar toolbar;
    private ListView content;
    private ContentAdapter adapter;

    // Content
    private String album;
    private ArrayList<String> container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Get values from bundle
        Intent mainContent = getIntent();
        Bundle bundle = mainContent.getExtras();
        album = bundle.getString("album");
        container = bundle.getStringArrayList("container");

        // Update controls
        toolbar = findViewById(R.id.menu_content);
        toolbar.inflateMenu(R.menu.menu_overview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(album);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        adapter = new ContentAdapter(context, container);
        content = findViewById(R.id.content);
        content.setEmptyView(findViewById(R.id.no_content));
        content.setAdapter(adapter);
        content.setOnItemClickListener((adapterView, view, i, l) -> showPreview(i));
    }

    private void showPreview(int pos) {
        Intent contentPreview = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("thumbnails", container);
        bundle.putInt("position", pos);
        contentPreview.putExtras(bundle);
        startActivityForResult(contentPreview, 97);
    }
}
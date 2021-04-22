package com.hcmus.apum;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private Context context;

    // GUI Controls
    private Toolbar toolbar;
    private ListView results;
    private SearchAdapter adapter;

    // Content
    private String query, scope;
    private ArrayList<String> resultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get values from bundle
        Intent mainSearch = getIntent();
        Bundle bundle = mainSearch.getExtras();
        query = bundle.getString("query");
        resultList = bundle.getStringArrayList("results");
        scope = bundle.getString("scope");

        // Update controls
        toolbar = findViewById(R.id.menu_search);
        toolbar.inflateMenu(R.menu.menu_preview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(scope + ":" + query);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        adapter = new SearchAdapter(context, resultList);
        results = findViewById(R.id.results);
        results.setEmptyView(findViewById(R.id.no_results));
        results.setAdapter(adapter);
        results.setOnItemClickListener((adapterView, view, i, l) -> showPreview(i));
    }

    private void showPreview(int pos) {
        Intent searchPreview = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("thumbnails", resultList);
        bundle.putInt("position", pos);
        searchPreview.putExtras(bundle);
        startActivityForResult(searchPreview, 97);
    }
}
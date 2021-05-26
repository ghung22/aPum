package com.hcmus.apum.component;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.SearchAdapter;
import com.hcmus.apum.tool.ActivityManager;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.*;

public class SearchActivity extends AppCompatActivity {
    private final Context context = SearchActivity.this;

    private String scope;
    private ArrayList<String> resultList;

    // Activity switching
    protected ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get values from bundle
        Intent mainSearch = getIntent();
        Bundle bundle = mainSearch.getExtras();
        String query = bundle.getString("query");
        resultList = bundle.getStringArrayList("results");
        scope = bundle.getString("scope");

        // Activity handler
        activityManager = new ActivityManager(this, "search");
        activityManager.setResult(bundle.getString("caller"));

        // Update controls
        Toolbar toolbar = findViewById(R.id.menu_search);
        toolbar.inflateMenu(R.menu.menu_overview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(String.format("\"%s\" in %s",
                    query, scope.substring(0, 1).toUpperCase() + scope.substring(1)));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        SearchAdapter adapter = new SearchAdapter(context, resultList, scope);
        ListView results = findViewById(R.id.results);
        results.setEmptyView(findViewById(R.id.no_media));
        results.setAdapter(adapter);
        results.setOnItemClickListener(this::resultClicked);
    }

    private void resultClicked(AdapterView<?> adapterView, View view, int i, long l) {
        if (fragNames.get(0).equals(scope)) {
            activityManager.showPreview(resultList, i);
        } else if (fragNames.get(1).equals(scope)) {
            String albumPath = mediaManager.getAlbums().get(i);
            ArrayList<String> container = mediaManager.getAlbumContent(albumPath);
            albumPath = albumPath.substring(albumPath.lastIndexOf("/") + 1);
            activityManager.showContent(albumPath, container);
        }
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
        finishActivity(SEARCH_REQUEST_CODE);
    }
}
package com.hcmus.apum.component;

import android.app.Activity;
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

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.CONTENT_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.SEARCH_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class SearchActivity extends AppCompatActivity {
    private final Context context = SearchActivity.this;

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
        toolbar.inflateMenu(R.menu.menu_overview);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(String.format("\"%s\" in %s",
                    query, scope.substring(0, 1).toUpperCase() + scope.substring(1)));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        adapter = new SearchAdapter(context, resultList, scope);
        results = findViewById(R.id.results);
        results.setEmptyView(findViewById(R.id.no_media));
        results.setAdapter(adapter);
        results.setOnItemClickListener(this::resultClicked);

        // Set values to return
        Intent searchMain = new Intent();
        Bundle returnBundle = new Bundle();
        returnBundle.putString("caller", bundle.getString("caller"));
        searchMain.putExtras(returnBundle);
        setResult(Activity.RESULT_OK, searchMain);
    }

    private void showContent(int pos) {
        String albumPath = mediaManager.getAlbums().get(pos);
        ArrayList<String> container = mediaManager.getAlbumContent(albumPath);
        albumPath = albumPath.substring(albumPath.lastIndexOf("/") + 1);

        Intent searchContent = new Intent(context, ContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "search");
        bundle.putString("album", albumPath);
        bundle.putStringArrayList("container", container);
        searchContent.putExtras(bundle);
        startActivityForResult(searchContent, CONTENT_REQUEST_CODE);
    }

    private void showPreview(int pos) {
        Intent searchPreview = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "search");
        bundle.putStringArrayList("thumbnails", resultList);
        bundle.putInt("position", pos);
        searchPreview.putExtras(bundle);
        startActivityForResult(searchPreview, 97);
    }

    private void resultClicked(AdapterView<?> adapterView, View view, int i, long l) {
        switch (scope) {
            case "overview":
                showPreview(i);
                break;
            case "albums":
                showContent(i);
                break;
            case "faces":
                break;
            case "favorite":
                break;
            default:
                break;
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(SEARCH_REQUEST_CODE);
    }
}
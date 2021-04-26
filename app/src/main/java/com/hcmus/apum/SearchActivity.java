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
    private Context context = SearchActivity.this;

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
            actionBar.setTitle(scope + ":" + query);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Update content
        adapter = new SearchAdapter(context, resultList, scope);
        results = findViewById(R.id.results);
        results.setEmptyView(findViewById(R.id.no_results));
        results.setAdapter(adapter);
        results.setOnItemClickListener((adapterView, view, i, l) -> showPreview(i));
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
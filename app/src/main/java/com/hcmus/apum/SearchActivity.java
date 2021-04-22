package com.hcmus.apum;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.mediaManager;

public class SearchActivity extends AppCompatActivity {
    Context context;

    // GUI Controls
    SearchAdapter adapter;

    // Content
    ListView results;
    ArrayList<String> resultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get Intent (search query)
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY),
            scope = intent.getStringExtra("SCOPE");
            resultList = mediaManager.search(query, scope);
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
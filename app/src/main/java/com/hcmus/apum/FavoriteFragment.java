package com.hcmus.apum;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.ABOUT_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.CAMERA_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.PREVIEW_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.SEARCH_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class FavoriteFragment extends Fragment {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private GridView grid;
    private GridAdapter adapter;

    // Search
    private MenuItem searchItem;
    private SearchView searchView;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_favorite, container, false);

        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);

        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new GridAdapter(getActivity(), mediaManager.getFavorites());
        grid = view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.no_media));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_favorite);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        menuAction(item);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_favorite, menu);

        // Get controls
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Search focused/unfocused events
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(final MenuItem item) {
                return menuShow(menu, false);
            }

            @Override
            public boolean onMenuItemActionCollapse(final MenuItem item) {
                return menuShow(menu, true);
            }
        });

        // Search query events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                ArrayList<String> results = mediaManager.search(query, "favorite");
                if (!results.isEmpty()) {
                    showSearch(query, results);
                } else {
                    Toast.makeText(getContext(), getContext().getText(R.string.err_search_not_found), Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Init icons
        searchView.setSubmitButtonEnabled(true);
        ImageView submitIcon = searchView.findViewById(androidx.appcompat.R.id.search_go_btn);
        submitIcon.setImageResource(R.drawable.ic_search);
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this.getContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "favorite");
        bundle.putStringArrayList("thumbnails", mediaManager.getImages());
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, PREVIEW_REQUEST_CODE);
    }

    private void showSearch(String query, ArrayList<String> results) {
        Intent mainSearch = new Intent(this.getContext(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "favorite");
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", "favorite");
        mainSearch.putExtras(bundle);
        startActivityForResult(mainSearch, SEARCH_REQUEST_CODE);
    }

    private boolean menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_search:
                searchItem.expandActionView();
                searchView.requestFocus();
                break;
            case R.id.action_select:
                break;
            case R.id.action_zoom:
                break;
            case R.id.action_sort:
                // TODO: Sort in Overview
                break;
            case R.id.action_reload:
                break;
            case R.id.action_trash:
                break;
            case R.id.action_vault:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent mainAbout = new Intent(this.getContext(), AboutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("caller", "favorite");
                mainAbout.putExtras(bundle);
                mainAbout.setFlags(0);
                startActivityForResult(mainAbout, ABOUT_REQUEST_CODE);
                break;
        }
        return true;
    }

    private void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem search = menu.findItem(R.id.action_search);
        if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }

    private boolean menuShow(Menu menu, boolean show) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != searchItem)
                item.setVisible(show);
        }
        return true;
    }
}
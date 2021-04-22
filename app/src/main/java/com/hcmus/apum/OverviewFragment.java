package com.hcmus.apum;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.File;

import static com.hcmus.apum.MainActivity.mediaManager;

public class OverviewFragment extends Fragment {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private GridView grid;
    private OverviewAdapter adapter;

    // Search
    private MenuItem searchItem;
    private SearchView searchView;

    // Test values

    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance(String param1, String param2) {
        OverviewFragment fragment = new OverviewFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ViewCompat.requestApplyInsets(view); // TODO: restore scroll state

        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new OverviewAdapter(getActivity(), mediaManager.getImages());
        grid = view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.empty));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this::menuAction);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this.getContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("thumbnails", mediaManager.getImages());
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, 97);
    }

    private static String getGalleryPath() {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/";
    }

    private boolean menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_add:
                Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePicIntent, 71);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(OverviewFragment.super.getContext(), getString(R.string.err_camera), Toast.LENGTH_LONG).show();
                }
                break;
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
                mediaManager.updateLocations(getContext());
                Toast.makeText(getContext(), "Image list reloaded.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_trash:
                break;
            case R.id.action_vault:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent mainAbout = new Intent(this.getContext(), AboutActivity.class);
                startActivityForResult(mainAbout, 46);
                break;
        }
        return true;
    }

    private void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem add = menu.findItem(R.id.action_add), search = menu.findItem(R.id.action_search);
        if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            add.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            add.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }
}
package com.hcmus.apum;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.File;
import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.mediaManager;
import static com.hcmus.apum.MainActivity.mediaPathList;

public class OverviewFragment extends Fragment {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private GridView grid;
    private ThumbnailAdapter adapter;

    // Test values
    final String[] items = {"Ant", "Baby", "Clown", "Duck", "Elephant", "Family", "Good", "Happy", "Igloo",
            "Jumping", "King", "Love", "Mother", "Napkin", "Orange", "Pillow"};
//    final int[] images = {R.drawable.ant, R.drawable.baby, R.drawable.clown, R.drawable.duck,
//            R.drawable.elephant, R.drawable.family, R.drawable.good, R.drawable.happy,
//            R.drawable.igloo, R.drawable.jumping, R.drawable.king, R.drawable.love,
//            R.drawable.mother, R.drawable.napkin, R.drawable.orange, R.drawable.pillow};

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
        appbar = (AppBarLayout) view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
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
        });
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsingToolbar);
        scroll = (NestedScrollView) view.findViewById(R.id.scroll);
        adapter = new ThumbnailAdapter(getActivity());
        grid = (GridView) view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.empty));
        grid.setAdapter(new ThumbnailAdapter(getActivity()));
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));

        // Init actionbar buttons
        toolbar = (Toolbar) view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this::menuAction);

        return view;
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this.getContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray("items", items);
        bundle.putStringArrayList("thumbnails", mediaPathList);
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, 97);
//        finish();
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
                break;
            case R.id.action_select:
                break;
            case R.id.action_zoom:
                break;
            case R.id.action_reload:
                break;
            case R.id.action_trash:
                break;
            case R.id.action_vault:
                break;
            case R.id.action_settings:
                break;
            case R.id.test:
                File img = new File(Uri.parse("android.resource://" + R.class.getPackage().getName() + "/res/drawable/" + "ant.jpg").toString());
                Toast.makeText(getContext(), getGalleryPath(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getContext(),img.toString(), Toast.LENGTH_LONG).show();
                Log.d("Test", img.getPath());
                break;
            case R.id.action_about:
                break;
        }
        return true;
    }
}
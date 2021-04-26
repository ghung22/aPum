package com.hcmus.apum;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.CAMERA_REQUEST_CODE;

public class FavoriteFragment extends Fragment {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private ListView list;
    private FavoriteAdapter adapter;

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
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorite, container, false);
        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Change icon to black/white depending on scroll state
                if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
                    toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                } else {
                    toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new FavoriteAdapter(getActivity());
        list = view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.no_media));
        list.setAdapter(adapter);

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_overview);
        toolbar.setOnMenuItemClickListener(this::menuAction);

        return view;
    }

    private boolean menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_add:
                Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePicIntent, CAMERA_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(FavoriteFragment.super.getContext(), getString(R.string.err_camera), Toast.LENGTH_LONG).show();
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
            case R.id.action_about:
                break;
        }
        return true;
    }
}
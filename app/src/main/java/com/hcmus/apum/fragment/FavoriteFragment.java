package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.fragNames;
import static com.hcmus.apum.MainActivity.mediaManager;

public class FavoriteFragment extends BaseFragment {
    public FavoriteFragment() {
        // Required empty public constructor
    }

    public static FavoriteFragment newInstance(ArrayList<String> mediaList) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("mediaList", mediaList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        caller = fragNames.get(3);
        searchScope = fragNames.get(3);
        layoutId = R.layout.fragment_favorite;
        menuId = R.menu.menu_favorite;
        mediaList = mediaManager.getFavorite();
        super.onCreate(savedInstanceState);
    }

    @Override
    public BaseAdapter initAdapter() {
        return new GridAdapter(requireContext(), mediaList);
    }

    @Override
    public void initContentLayout(View view) {
        GridView grid = view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.no_media));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));
    }

    @Override
    public void initToolbar(View view) {
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_favorite);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        ((GridAdapter) adapter).addAll(mediaList);
    }
}
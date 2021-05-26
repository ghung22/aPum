package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hcmus.apum.R;
import com.hcmus.apum.adapter.AlbumAdapter;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.fragNames;
import static com.hcmus.apum.MainActivity.mediaManager;

public class AlbumsFragment extends BaseFragment {
    public AlbumsFragment() {
        // Required empty public constructor
    }

    public static AlbumsFragment newInstance(ArrayList<String> mediaList) {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("mediaList", mediaList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        caller = fragNames.get(1);
        searchScope = fragNames.get(1);
        layoutId = R.layout.fragment_albums;
        menuId = R.menu.menu_albums;
        super.onCreate(savedInstanceState);
    }

    @Override
    public BaseAdapter initAdapter() {
        ArrayList<Integer> mediaCountList = mediaManager.getAlbumCounts(mediaList);
        return new AlbumAdapter(requireContext(), mediaList, mediaCountList);
    }

    @Override
    public void initContentLayout(View view) {
        ListView list = view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.no_media));
        list.setAdapter(adapter);
        list.setOnItemClickListener(this::showContent);
    }

    @Override
    public void initToolbar(View view) {
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_albums);
        toolbar.setOnMenuItemClickListener(this::menuActionBool);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        if (mediaCountList == null) {
            return;
        }
        ((AlbumAdapter) adapter).addAll(mediaList, mediaCountList);
    }

    @Override
    protected ArrayList<String> getContent() {
        return mediaManager.getAlbumContent(selectedOption);
    }
}
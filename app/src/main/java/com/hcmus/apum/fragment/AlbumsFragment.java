package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.AlbumAdapter;

import java.util.ArrayList;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_albums, container, false);

        // Init data
        if (getArguments() != null) {
            mediaList = getArguments().getStringArrayList("mediaList");
        }
        // Data
        ArrayList<Integer> mediaCountList = mediaManager.getAlbumCounts(mediaList);
        caller = "albums";
        searchScope = "albums";

        // Init controls
            // GUI controls
        AppBarLayout appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        adapter = new AlbumAdapter(getActivity(), mediaList, mediaCountList);
        ListView list = view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.no_media));
        list.setAdapter(adapter);
        list.setOnItemClickListener(this::showContent);
            // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_albums);
        toolbar.setOnMenuItemClickListener(this::menuActionBool);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        if (mediaCountList == null) {
            return;
        }
        ((AlbumAdapter) adapter).addAll(mediaList, mediaCountList);
    }

    @Override
    public void inflateOptionMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_albums, menu);
    }

    @Override
    protected ArrayList<String> getContent() {
        return mediaManager.getAlbumContent(selectedOption);
    }
}
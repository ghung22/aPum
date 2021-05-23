package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.GridView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;

import java.util.ArrayList;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Init data
//        mediaList = getArguments().getStringArrayList("mediaList");
        mediaList = mediaManager.getFavorite();

        // Init controls
        // GUI controls
        AppBarLayout appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);

        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        adapter = new GridAdapter(getActivity(), mediaList);

        GridView grid = view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.no_media));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_favorite);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        ((GridAdapter) adapter).addAll(mediaList);
    }

    @Override
    public void inflateOptionMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }
}
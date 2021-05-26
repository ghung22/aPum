package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.hcmus.apum.MainActivity.fragNames;
import static com.hcmus.apum.MainActivity.mediaManager;

public class FacesFragment extends BaseFragment {
    // Data
    private HashMap<String, ArrayList<String>> faceList = new HashMap<>();

    public FacesFragment() {
        // Required empty public constructor
    }

    public static FacesFragment newInstance(ArrayList<String> mediaList) {
        FacesFragment fragment = new FacesFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("mediaList", mediaList);
        fragment.setArguments(args);
        return fragment;
    }

    public GridAdapter getAdapter() {
        return (GridAdapter) adapter;
    }

    public Menu getMenu() {
        return toolbar.getMenu();
    }

    public void addMediaFace(String media, ArrayList<String> face) {
        faceList.put(media, face);
        ((GridAdapter) adapter).add(media);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        caller = fragNames.get(2);
        searchScope = fragNames.get(2);
        layoutId = R.layout.fragment_faces;
        menuId = R.menu.menu_faces;

        // Override data
        faceList = mediaManager.getFaceData();
        mediaList = new ArrayList<>(faceList.keySet());

        super.onCreate(savedInstanceState);
    }

    @Override
    public BaseAdapter initAdapter() {
        return new GridAdapter(requireContext(), mediaList);
    }

    @Override
    public void initContentLayout(View view) {
        GridView grid = view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this::showContent);

        // Empty view of grid
        Button faces_no_faces_btn = view.findViewById(R.id.faces_no_faces_btn);
        faces_no_faces_btn.setOnClickListener(view1 -> regenerate());
        grid.setEmptyView(view.findViewById(R.id.faces_no_faces));
    }

    @Override
    public void initToolbar(View view) {
        toolbar = view.findViewById(R.id.menu_faces);
        toolbar.inflateMenu(R.menu.menu_faces);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        ((GridAdapter) adapter).addAll(mediaList);
    }

    @Override
    protected ArrayList<String> getContent() {
        return faceList.get(selectedOption);
    }
}
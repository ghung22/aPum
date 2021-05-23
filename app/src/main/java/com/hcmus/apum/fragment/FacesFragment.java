package com.hcmus.apum.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.GridView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.android.material.appbar.AppBarLayout;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;

import java.util.ArrayList;
import java.util.HashMap;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faces, container, false);
        ViewCompat.requestApplyInsets(view);

        // Init data
        if (getArguments() != null) {
            mediaList = getArguments().getStringArrayList("mediaList");
        }
        faceList = mediaManager.getFaceData();
        mediaList = new ArrayList<>(faceList.keySet());

        // Init controls
        // GUI controls
        AppBarLayout appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        adapter = new GridAdapter(getActivity(), mediaList);
        GridView grid = view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this::showContent);
        // Empty view of grid
        Button faces_no_faces_btn = view.findViewById(R.id.faces_no_faces_btn);
        faces_no_faces_btn.setOnClickListener(view1 -> regenerate());
        grid.setEmptyView(view.findViewById(R.id.faces_no_faces));
        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_faces);
        toolbar.inflateMenu(R.menu.menu_faces);
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
        inflater.inflate(R.menu.menu_faces, menu);
    }

    @Override
    protected ArrayList<String> getContent() {
        return faceList.get(selectedOption);
    }
}
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

public class OverviewFragment extends BaseFragment {
    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance(ArrayList<String> mediaList) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("mediaList", mediaList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        caller = fragNames.get(0);
        searchScope = fragNames.get(0);
        layoutId = R.layout.fragment_overview;
        menuId = R.menu.menu_overview;
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
        toolbar.inflateMenu(R.menu.menu_overview);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    @Override
    public void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList) {
        ((GridAdapter) adapter).addAll(mediaList);
    }
}
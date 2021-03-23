package com.hcmus.apum;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toolbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment {

    // GUI controls
    Toolbar toolbar;
    GridView grid;

    // Test values
    private final String[] items = {"Ant","Baby","Clown", "Duck", "Elephant", "Family", "Good", "Happy", "Igloo",
            "Jumping", "King", "Love", "Mother", "Napkin", "Orange", "Pillow"};
    private final int[] images = {R.drawable.ant, R.drawable.baby, R.drawable.clown, R.drawable.duck,
            R.drawable.elephant, R.drawable.family, R.drawable.good, R.drawable.happy,
            R.drawable.igloo, R.drawable.jumping, R.drawable.king, R.drawable.love,
            R.drawable.mother, R.drawable.napkin, R.drawable.orange, R.drawable.pillow};

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init controls
        if (getArguments() != null) {
            grid = (GridView) getView().findViewById(R.id.grid);
            grid.setAdapter(new ThumbnailAdapter(this.getContext(), images));
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showPreview(i);
                }
            });
        }

        // Init actionbar buttons
        toolbar = (Toolbar) getView().findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_main);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this.getContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray("items", items);
        bundle.putIntArray("thumbnails", images);
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, 97);
//        finish();
    }
}
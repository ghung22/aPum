package com.hcmus.apum;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
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
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add:
                        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        try {
                            startActivityForResult(takePicIntent, 71);
                        } catch (ActivityNotFoundException e ) {
                            Toast.makeText(OverviewFragment.super.getContext(), "(!) Problem opening the camera", 5000).show();
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
        });
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
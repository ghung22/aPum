package com.hcmus.apum.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hcmus.apum.AboutActivity;
import com.hcmus.apum.FragmentCallbacks;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;
import com.hcmus.apum.component.PreviewActivity;
import com.hcmus.apum.component.SearchActivity;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.*;

public class OverviewFragment extends Fragment implements FragmentCallbacks {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private GridView grid;
    private GridAdapter adapter;

    // Search
    private MenuItem searchItem;
    private SearchView searchView;

    // Data
    private ArrayList<String> mediaList = new ArrayList<>();

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ViewCompat.requestApplyInsets(view);

        // Init data
        mediaList = getArguments().getStringArrayList("mediaList");

        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new GridAdapter(getActivity(), mediaList);
        grid = view.findViewById(R.id.grid);
        grid.setEmptyView(view.findViewById(R.id.no_media));
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((adapterView, view1, i, l) -> showPreview(i));

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_overview);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        menuAction(item);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_overview, menu);

        // Get controls
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // Search focused/unfocused events
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(final MenuItem item) {
                return menuShow(menu, false);
            }

            @Override
            public boolean onMenuItemActionCollapse(final MenuItem item) {
                return menuShow(menu, true);
            }
        });

        // Search query events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                ArrayList<String> results = mediaManager.search(query, "overview");
                if (!results.isEmpty()) {
                    showSearch(query, results);
                } else {
                    Toast.makeText(getContext(), getContext().getText(R.string.err_search_not_found), Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Init icons
        searchView.setSubmitButtonEnabled(true);
        ImageView submitIcon = searchView.findViewById(androidx.appcompat.R.id.search_go_btn);
        submitIcon.setImageResource(R.drawable.ic_search);
    }

    private void showPreview(int pos) {
        Intent mainPreview = new Intent(this.getContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "overview");
        bundle.putStringArrayList("thumbnails", mediaList);
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, PREVIEW_REQUEST_CODE);
    }

    private void showSearch(String query, ArrayList<String> results) {
        Intent mainSearch = new Intent(this.getContext(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "overview");
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", "overview");
        mainSearch.putExtras(bundle);
        startActivityForResult(mainSearch, SEARCH_REQUEST_CODE);
    }

    private static String getGalleryPath() {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/";
    }

    private boolean menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_add:
                Intent overviewCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(overviewCamera, CAMERA_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(OverviewFragment.super.getContext(), getString(R.string.err_camera), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_search:
                searchItem.expandActionView();
                searchView.requestFocus();
                break;
            case R.id.action_sort:
                mediaManager.sortUI(getContext(), "overview", mediaList);
                break;
            case R.id.action_reload:
                mediaManager.updateLocations(getContext());
                mediaList = mediaManager.sort(mediaManager.getMedia(), "date", false);
                adapter.addAll(mediaList);
                Toast.makeText(getContext(), getString(R.string.info_overview_reload), Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_about:
                Intent mainAbout = new Intent(this.getContext(), AboutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("caller", "overview");
                mainAbout.putExtras(bundle);
                mainAbout.setFlags(0);
                startActivityForResult(mainAbout, ABOUT_REQUEST_CODE);
                break;
            default:
                Toast.makeText(getContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem add = menu.findItem(R.id.action_add), search = menu.findItem(R.id.action_search);
        if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            add.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            add.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }

    private boolean menuShow(Menu menu, boolean show) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != searchItem)
                item.setVisible(show);
        }
        return true;
    }

    @Override
    public void mainToFrag(Bundle bundle) {
        String action = bundle.getString("action");
        if (action != null) {
            switch (action) {
                case "sort":
                case "reload":
                    mediaList = bundle.getStringArrayList("mediaList");
                    adapter.addAll(mediaList);
                    break;
                default:
                    break;
            }
        }
    }
}
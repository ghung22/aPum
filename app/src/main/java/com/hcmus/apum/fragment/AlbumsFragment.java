package com.hcmus.apum.fragment;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hcmus.apum.AboutActivity;
import com.hcmus.apum.FragmentCallbacks;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.AlbumAdapter;
import com.hcmus.apum.component.ContentActivity;
import com.hcmus.apum.component.SearchActivity;

import java.util.ArrayList;

import static com.hcmus.apum.MainActivity.ABOUT_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.CONTENT_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.SEARCH_REQUEST_CODE;
import static com.hcmus.apum.MainActivity.mediaManager;

public class AlbumsFragment extends Fragment implements FragmentCallbacks {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private ListView list;
    private AlbumAdapter adapter;

    // Search
    private MenuItem searchItem;
    private SearchView searchView;

    // Data
    private ArrayList<String> mediaList = new ArrayList<>();
    private ArrayList<Integer> mediaCountList = new ArrayList<>();

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
        mediaList = getArguments().getStringArrayList("mediaList");
        mediaCountList = mediaManager.getAlbumCounts(mediaList);

        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new AlbumAdapter(getActivity(), mediaList, mediaCountList);
        list = view.findViewById(R.id.list);
        list.setEmptyView(view.findViewById(R.id.no_media));
        list.setAdapter(adapter);
        list.setOnItemClickListener(this::showContent);

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_main);
        toolbar.inflateMenu(R.menu.menu_albums);
        toolbar.setOnMenuItemClickListener(this::menuAction);
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
        inflater.inflate(R.menu.menu_albums, menu);

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
                ArrayList<String> results = mediaManager.search(query, "albums");
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

    private void showContent(AdapterView<?> parent, View view, int pos, long id) {
        String albumPath = mediaList.get(pos);
        ArrayList<String> container = mediaManager.getAlbumContent(albumPath);
        albumPath = albumPath.substring(albumPath.lastIndexOf("/") + 1);

        Intent mainContent = new Intent(this.getContext(), ContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "albums");
        bundle.putString("host", albumPath);
        bundle.putStringArrayList("container", container);
        mainContent.putExtras(bundle);
        startActivityForResult(mainContent, CONTENT_REQUEST_CODE);
    }

    private void showSearch(String query, ArrayList<String> results) {
        Intent mainSearch = new Intent(this.getContext(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "albums");
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", "albums");
        mainSearch.putExtras(bundle);
        startActivityForResult(mainSearch, SEARCH_REQUEST_CODE);
    }

    private boolean menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_search:
                searchItem.expandActionView();
                searchView.requestFocus();
                break;
            case R.id.action_sort:
                mediaManager.sortUI(getContext(), "albums", mediaList);
                break;
            case R.id.action_reload:
                mediaManager.updateLocations(getContext());
                mediaList = mediaManager.sort(mediaManager.getAlbums(), "name");
                mediaCountList = mediaManager.getAlbumCounts(mediaList);
                adapter.addAll(mediaList, mediaCountList);
                Toast.makeText(getContext(), getString(R.string.info_albums_reload), Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_trash:
                break;
            case R.id.action_vault:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent mainAbout = new Intent(this.getContext(), AboutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("caller", "albums");
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
        MenuItem search = menu.findItem(R.id.action_search);
        if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
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
                    mediaCountList = mediaManager.getAlbumCounts(mediaList);
                    adapter.addAll(mediaList, mediaCountList);
                    break;
                default:
                    break;
            }
        }
    }
}
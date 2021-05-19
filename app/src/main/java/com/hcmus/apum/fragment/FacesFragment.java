package com.hcmus.apum.fragment;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.*;
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
import com.hcmus.apum.MediaManager;
import com.hcmus.apum.R;
import com.hcmus.apum.adapter.GridAdapter;
import com.hcmus.apum.component.ContentActivity;
import com.hcmus.apum.component.SearchActivity;

import java.util.ArrayList;
import java.util.HashMap;

import static com.hcmus.apum.MainActivity.*;

public class FacesFragment extends Fragment implements FragmentCallbacks {

    // GUI controls
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private NestedScrollView scroll;
    private GridView grid;
    private GridAdapter adapter;
    private Button faces_no_faces_btn;
    private ImageView action_regenerate;

    // Search
    private MenuItem searchItem;
    private SearchView searchView;

    // Data
    private ArrayList<String> mediaList = new ArrayList<>();
    private HashMap<String, ArrayList<Rect>> faceList = new HashMap<>();
    boolean toolbarCollapsed = false;

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
        return adapter;
    }
    public Menu getMenu() {
        return toolbar.getMenu();
    }

    public void addMediaFace(String media, ArrayList<Rect> face) {
//        mediaList.add(media);
        faceList.put(media, face);
        adapter.add(media);
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
        mediaList = getArguments().getStringArrayList("mediaList");
        faceList = mediaManager.getFaceData(mediaList);
        mediaList = new ArrayList<>(faceList.keySet());

        // Init controls
        appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        scroll = view.findViewById(R.id.scroll);
        adapter = new GridAdapter(getActivity(), mediaList);
        grid = view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this::showContent);
        // Empty view of grid
        faces_no_faces_btn = view.findViewById(R.id.faces_no_faces_btn);
        faces_no_faces_btn.setOnClickListener(view1 -> regenerate());
        grid.setEmptyView(view.findViewById(R.id.faces_no_faces));
        action_regenerate = (ImageView) inflater.inflate(R.layout.layout_refresh_icon, null);

        // Init actionbar buttons
        toolbar = view.findViewById(R.id.menu_faces);
        toolbar.inflateMenu(R.menu.menu_faces);
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
        inflater.inflate(R.menu.menu_faces, menu);

        // Get controls
        menu.findItem(R.id.action_regenerate).setActionView(action_regenerate);
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
                ArrayList<String> results = mediaManager.search(query, "faces");
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

    private void showContent(AdapterView<?> parent, View view, int pos, long id) {
        String img = mediaList.get(pos);
        ArrayList<String> container = new ArrayList<>();
        // Create a list of rect values in this format: <left>,<top>,<right>,<bottom>
        for (Rect rect : faceList.get(img)) {
            container.add(rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
        }

        Intent mainContent = new Intent(this.getContext(), ContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "faces");
        bundle.putString("host", img);
        bundle.putStringArrayList("container", container);
        mainContent.putExtras(bundle);
        startActivityForResult(mainContent, CONTENT_REQUEST_CODE);
    }

    private void showSearch(String query, ArrayList<String> results) {
        Intent mainSearch = new Intent(this.getContext(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "faces");
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", "faces");
        mainSearch.putExtras(bundle);
        startActivityForResult(mainSearch, SEARCH_REQUEST_CODE);
    }

    private void menuAction(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_regenerate:
                regenerate();
                break;
            case R.id.action_search:
                searchItem.expandActionView();
                searchView.requestFocus();
                break;
            case R.id.action_sort:
                mediaManager.sortUI(getContext(), "faces", mediaList);
                break;
            case R.id.action_about:
                Intent mainAbout = new Intent(this.getContext(), AboutActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("caller", "faces");
                mainAbout.putExtras(bundle);
                mainAbout.setFlags(0);
                startActivityForResult(mainAbout, ABOUT_REQUEST_CODE);
                break;
            default:
                Toast.makeText(getContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        menuRecolor(
                (collapsingToolbar.getHeight() + verticalOffset) <
                (collapsingToolbar.getScrimVisibleHeightTrigger())
        );
    }

    private void menuRecolor(boolean toolbarCollapsed) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem regenerate = menu.findItem(R.id.action_regenerate), search = menu.findItem(R.id.action_search);
        if (toolbarCollapsed) {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            regenerate.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            toolbar.getOverflowIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            regenerate.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            search.getIcon().setColorFilter(getContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
        this.toolbarCollapsed = toolbarCollapsed;
    }

    private boolean menuShow(Menu menu, boolean show) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != searchItem)
                item.setVisible(show);
        }
        return true;
    }

    private void regenerate() {
        MediaManager.AsyncFacesUpdater updater = mediaManager.updateFaces(getContext(), this);
        Thread thread = new Thread(() -> {
            Menu menu = toolbar.getMenu();
            MenuItem regenerate = menu.findItem(R.id.action_regenerate);
            // Animate generate icon TODO: Not animated yet
            RotateAnimation rotate = new RotateAnimation(
                    0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(1000);
            rotate.setRepeatCount(Animation.INFINITE);
            while (true) {
                if (updater.getStatus() == AsyncTask.Status.RUNNING) {
                    if (regenerate.getActionView().getAnimation() == null) {
                        regenerate.getActionView().startAnimation(rotate);
                        menuRecolor(toolbarCollapsed);
                    }
                } else if (updater.getStatus() == AsyncTask.Status.FINISHED){
                    regenerate.getActionView().clearAnimation();
                    menuRecolor(toolbarCollapsed);
                    break;
                }
            }
        });
        thread.start();
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
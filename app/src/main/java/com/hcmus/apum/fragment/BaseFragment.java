package com.hcmus.apum.fragment;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hcmus.apum.FragmentCallbacks;
import com.hcmus.apum.MainActivity;
import com.hcmus.apum.R;
import com.hcmus.apum.tool.ActivityManager;

import java.util.ArrayList;
import java.util.Arrays;

import static com.hcmus.apum.MainActivity.mediaManager;

public abstract class BaseFragment extends Fragment implements FragmentCallbacks {
    // Layout
    protected int layoutId, menuId;
    protected BaseAdapter adapter;
    protected CollapsingToolbarLayout collapsingToolbar;
    protected Toolbar toolbar;

    // Data
    protected ArrayList<String> mediaList;
    protected String caller, selectedOption;

    // Search
    protected MenuItem searchItem;
    protected SearchView searchView;
    protected String searchScope;
    
    // Activity switching
    protected ActivityManager activityManager;

    /**
     * Create an adapter for parsing content into a layout
     * Some implemented adapters: GridAdapter, AlbumAdapter
     * @return an adapter extends from BaseAdapter
     */
    public abstract BaseAdapter initAdapter();

    /**
     * Get the content layout in this fragment, set its empty view, adapter, and any event if available
     * @param view the inflated view of this fragment
     */
    public abstract void initContentLayout(View view);

    /**
     * Get the toolbar in this fragment, inflate a menu, and set it as a SupportActionBar
     * @param view the inflated view of this fragment
     */
    public abstract void initToolbar(View view);

    /**
     * Replace old content with a new one in the initialized adapter to reflect data changes on the UI
     * @param mediaList the new content
     * @param mediaCountList item counts of each album (only for AlbumsFragment)
     */
    public abstract void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList);

    protected ArrayList<String> getContent() {
        return new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager = new ActivityManager(this, caller);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(layoutId, container, false);
        ViewCompat.requestApplyInsets(view);

        // Init data
        if (getArguments() != null) {
            mediaList = getArguments().getStringArrayList("mediaList");
        }

        // Init controls
        AppBarLayout appbar = view.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this::menuRecolor);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        adapter = initAdapter();
        initContentLayout(view);
        initToolbar(view);

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
        inflater.inflate(menuId, menu);

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
                ArrayList<String> results = mediaManager.search(query, searchScope);
                if (!results.isEmpty()) {
                    showSearch(query, results);
                } else {
                    Toast.makeText(requireContext(), requireContext().getText(R.string.err_search_not_found), Toast.LENGTH_SHORT).show();
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

    protected boolean menuShow(Menu menu, boolean show) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != searchItem)
                item.setVisible(show);
        }
        return true;
    }

    @SuppressWarnings("unused")
    protected void showContent(AdapterView<?> parent, View view, int pos, long id) {
        selectedOption = mediaList.get(pos);
        ArrayList<String> container = getContent();
        selectedOption = selectedOption.substring(selectedOption.lastIndexOf("/") + 1);
        activityManager.showContent(selectedOption, container);
    }

    protected void showPreview(int pos) {
        activityManager.showPreview(mediaList, pos);
    }

    protected void showSearch(String query, ArrayList<String> results) {
        activityManager.showSearch(query, searchScope, results);
    }

    protected void menuAction(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_add) {
            activityManager.showCamera();
        } else if (itemId == R.id.action_regenerate) {
            regenerate();
        } else if (itemId == R.id.action_search) {
            searchItem.expandActionView();
            searchView.requestFocus();
        } else if (itemId == R.id.action_sort) {
            mediaManager.sortUI(requireContext(), caller, mediaList);
        } else if (itemId == R.id.action_reload) {
            Bundle bundle = new Bundle();
            bundle.putString("caller", caller);
            bundle.putString("action", "reload");
            ((MainActivity) requireContext()).fragToMain(caller, bundle);
        } else if (itemId == R.id.action_about) {
            activityManager.showAbout();
        } else {
            Toast.makeText(requireContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean menuActionBool(MenuItem menuItem) {
        try {
            menuAction(menuItem);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem
                regenerate = menu.findItem(R.id.action_regenerate),
                add = menu.findItem(R.id.action_add),
                search = menu.findItem(R.id.action_search);

        // Get filter
        PorterDuffColorFilter filter = null;
        if ((collapsingToolbar.getHeight() + verticalOffset)
                < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            filter = new PorterDuffColorFilter(requireContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else if ((requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                != Configuration.UI_MODE_NIGHT_YES) {
            filter = new PorterDuffColorFilter(requireContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }

        // Apply filter
        if (filter != null) {
            if (toolbar.getOverflowIcon() != null) {
                toolbar.getOverflowIcon().setColorFilter(filter);
            }
            for (MenuItem menuItem : Arrays.asList(add, regenerate, search)) {
                if (menuItem != null) {
                    menuItem.getIcon().setColorFilter(filter);
                }
            }
        }
    }

    /**
     * Create a new face data (only for FaceFragment)
     */
    protected void regenerate() {
        mediaManager.updateFaces(requireContext(), (FacesFragment) this);
    }

    @Override
    public void mainToFrag(Bundle bundle) {
        String action = bundle.getString("action");
        if (action != null) {
            switch (action) {
                case "sort":
                case "reload":
                    mediaList = bundle.getStringArrayList("mediaList");
                    updateAdapter(mediaList, null);
                    break;
                default:
                    break;
            }
        }
    }
}
package com.hcmus.apum.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hcmus.apum.AboutActivity;
import com.hcmus.apum.FragmentCallbacks;
import com.hcmus.apum.MainActivity;
import com.hcmus.apum.R;
import com.hcmus.apum.component.ContentActivity;
import com.hcmus.apum.component.PreviewActivity;
import com.hcmus.apum.component.SearchActivity;

import java.util.ArrayList;
import java.util.Arrays;

import static com.hcmus.apum.MainActivity.*;

public abstract class BaseFragment extends Fragment implements FragmentCallbacks {
    // Layout
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

    public abstract void updateAdapter(ArrayList<String> mediaList, @Nullable ArrayList<Integer> mediaCountList);
    public abstract void inflateOptionMenu(Menu menu, MenuInflater inflater);
    protected ArrayList<String> getContent() {
        return new ArrayList<>();
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
        inflateOptionMenu(menu, inflater);

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

    protected void showContent(AdapterView<?> parent, View view, int pos, long id) {
        selectedOption = mediaList.get(pos);
        ArrayList<String> container = getContent();
        selectedOption = selectedOption.substring(selectedOption.lastIndexOf("/") + 1);

        Intent mainContent = new Intent(requireContext(), ContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putString("host", selectedOption);
        bundle.putStringArrayList("container", container);
        mainContent.putExtras(bundle);
        startActivityForResult(mainContent, CONTENT_REQUEST_CODE);
    }

    protected void showPreview(int pos) {
        Intent mainPreview = new Intent(requireContext(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putStringArrayList("thumbnails", mediaList);
        bundle.putInt("position", pos);
        mainPreview.putExtras(bundle);
        startActivityForResult(mainPreview, PREVIEW_REQUEST_CODE);
    }

    protected void showSearch(String query, ArrayList<String> results) {
        Intent mainSearch = new Intent(requireContext(), SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller);
        bundle.putString("query", query);
        bundle.putStringArrayList("results", results);
        bundle.putString("scope", searchScope);
        mainSearch.putExtras(bundle);
        startActivityForResult(mainSearch, SEARCH_REQUEST_CODE);
    }

    protected void menuAction(MenuItem menuItem) {
        Bundle bundle;
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_add) {
            Intent overviewCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(overviewCamera, CAMERA_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), getString(R.string.err_camera), Toast.LENGTH_LONG).show();
            }
        } else if (itemId == R.id.action_regenerate) {
            regenerate();
        } else if (itemId == R.id.action_search) {
            searchItem.expandActionView();
            searchView.requestFocus();
        } else if (itemId == R.id.action_sort) {
            mediaManager.sortUI(requireContext(), caller, mediaList);
        } else if (itemId == R.id.action_reload) {
            bundle = new Bundle();
            bundle.putString("caller", caller);
            bundle.putString("action", "reload");
            ((MainActivity) requireContext()).fragToMain(caller, bundle);
        } else if (itemId == R.id.action_about) {
            Intent mainAbout = new Intent(requireContext(), AboutActivity.class);
            bundle = new Bundle();
            bundle.putString("caller", caller);
            mainAbout.putExtras(bundle);
            mainAbout.setFlags(0);
            startActivityForResult(mainAbout, ABOUT_REQUEST_CODE);
        } else {
            Toast.makeText(requireContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean menuActionBool(MenuItem menuItem) {
        menuAction(menuItem);
        return true;
    }

    protected void menuRecolor(AppBarLayout appBarLayout, int verticalOffset) {
        // Change icon to black/white depending on scroll state
        Menu menu = toolbar.getMenu();
        MenuItem
                regenerate = menu.findItem(R.id.action_regenerate),
                add = menu.findItem(R.id.action_add),
                search = menu.findItem(R.id.action_search);
        if ((collapsingToolbar.getHeight() + verticalOffset) < (collapsingToolbar.getScrimVisibleHeightTrigger())) {
            toolbar.getOverflowIcon().setColorFilter(requireContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            for (MenuItem menuItem : Arrays.asList(add, regenerate, search)) {
                if (menuItem != null) {
                    menuItem.getIcon().setColorFilter(requireContext().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                }
            }
        } else if ((requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                != Configuration.UI_MODE_NIGHT_YES) {
            toolbar.getOverflowIcon().setColorFilter(requireContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            for (MenuItem menuItem : Arrays.asList(add, regenerate, search)) {
                if (menuItem != null) {
                    menuItem.getIcon().setColorFilter(requireContext().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }

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
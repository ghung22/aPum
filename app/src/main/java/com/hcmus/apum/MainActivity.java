package com.hcmus.apum;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.database.SQLException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmus.apum.fragment.AlbumsFragment;
import com.hcmus.apum.fragment.FacesFragment;
import com.hcmus.apum.fragment.FavoriteFragment;
import com.hcmus.apum.fragment.OverviewFragment;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainCallbacks {

    // Static objects
    public static MediaManager mediaManager = new MediaManager();
    public static Boolean debugEnabled = true;

    // Request codes
    public static int PREVIEW_REQUEST_CODE = 97;
    public static int CONTENT_REQUEST_CODE = 27;
    public static int SEARCH_REQUEST_CODE = 5;
    public static int CAMERA_REQUEST_CODE = 71;
    public static int ABOUT_REQUEST_CODE = 46;
    public static int CHOOSER_REQUEST_CODE = 75;

    // GUI controls
    private BottomNavigationView navBar;

    // Fragments
    private OverviewFragment overview;
    private AlbumsFragment albums;
    private FacesFragment faces;
    private FavoriteFragment favorite;

    // For use/save state values
    private Bundle savedInstanceState;

    //Database
    public static DatabaseFavorites db_fav;

    // For threads
    private String currentFragment = "overview";
    private ArrayList<String> overviewData, albumsData, favoriteData,
            newOverviewData, newAlbumsData, newFavoriteData;
    private AsyncUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        // Init data
        mediaManager.updateLocations(this);
        mediaManager.updateFavoriteLocations(this);

        //Database
        db_fav = new DatabaseFavorites(this);
        try {
            db_fav.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            db_fav.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        // Init fragments
        overview = OverviewFragment.newInstance(mediaManager.sort(mediaManager.getImages(), "date", false));
        albums = AlbumsFragment.newInstance(mediaManager.sort(mediaManager.getAlbums(), "name"));
        faces = FacesFragment.newInstance(mediaManager.sort(mediaManager.getFaces(), "date", false));
        favorite = FavoriteFragment.newInstance(mediaManager.sort(db_fav.getAllFavorite(), "date", false));

        overviewData = mediaManager.sort(mediaManager.getImages(), "date", false);
        albumsData = mediaManager.sort(mediaManager.getAlbums(), "name");
        favoriteData = mediaManager.sort(mediaManager.getFaces(), "date", false);
        newOverviewData = overviewData;
        newAlbumsData = albumsData;
        newFavoriteData = favoriteData;

        // Init fragments
        overview = OverviewFragment.newInstance(overviewData);
        albums = AlbumsFragment.newInstance(albumsData);
        faces = FacesFragment.newInstance(overviewData);
        favorite = FavoriteFragment.newInstance(favoriteData);


        // Init GUI
        FragmentTransaction ft_main = getSupportFragmentManager().beginTransaction();
        ft_main.replace(R.id.frame, overview);
        ft_main.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft_main.addToBackStack(null);
        ft_main.commit();

        // Init controls
        navBar = findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(item -> switchFragment(item.getItemId()));

        // Init updater
        updater = new AsyncUpdater();
        updater.execute();
        db_fav.close();
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        updater.cancel(true);
//    }

    private boolean switchFragment(int itemId) {
        FragmentTransaction ft_navBar = getSupportFragmentManager().beginTransaction();
        switch (itemId) {
            case R.id.action_overview:
                ft_navBar.replace(R.id.frame, overview);
                currentFragment = "overview";
                break;
            case R.id.action_albums:
                ft_navBar.replace(R.id.frame, albums);
                currentFragment = "albums";
                break;
            case R.id.action_faces:
                ft_navBar.replace(R.id.frame, faces);
                currentFragment = "face";
                break;
            case R.id.action_favorite:
                ft_navBar.replace(R.id.frame, favorite);
                currentFragment = "favorite";
                break;
        }
        ft_navBar.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft_navBar.addToBackStack(null);
        ft_navBar.commit();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for which activity returned to MainActivity
        if (requestCode == CAMERA_REQUEST_CODE) {
            switchFragment(R.id.action_overview);
        } else {
            // When return data exists
            if (data != null) {
                if (data.hasExtra("caller")) {
                    String caller = data.getStringExtra("caller");
                    switch (caller) {
                        case "albums":
                            switchFragment(R.id.action_albums);
                            break;
                        case "faces":
                            switchFragment(R.id.action_faces);
                            break;
                        case "favorite":
                            switchFragment(R.id.action_favorite);
                            break;
                        default:
                            switchFragment(R.id.action_overview);
                            break;
                    }
                    return;
                }
            } else {
                switchFragment(R.id.action_overview);
            }
        }
    }

    @Override
    public void fragToMain(String caller, Bundle bundle) {
        // Forward bundle to fragments
        switch (caller) {
            case "overview":
                overview.mainToFrag(bundle);
                break;
            case "albums":
                albums.mainToFrag(bundle);
                break;
            case "faces":
                faces.mainToFrag(bundle);
                break;
            case "favorite":
                favorite.mainToFrag(bundle);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public class AsyncUpdater extends AsyncTask<String, String, String> {
        private final Bundle bundle = new Bundle();
        private final String TAG = "ASYNC_UPDATER";
        private final int UPDATE_INTERVAL = 10000;

        @Override
        protected void onPreExecute() {
            bundle.putString("action", "reload");
        }

        @Override
        protected String doInBackground(String... params) {
            while (true) {
                try {
                    mediaManager.updateLocations(MainActivity.this);
                    mediaManager.updateFavoriteLocations(MainActivity.this);
                    newOverviewData = mediaManager.sort(mediaManager.getImages(), "date", false);
                    newAlbumsData = mediaManager.sort(mediaManager.getAlbums(), "name");
                    newFavoriteData = mediaManager.sort(mediaManager.getFaces(), "date", false);
                    switch (currentFragment) {
                        case "overview":
                            if (!newOverviewData.equals(overviewData)) {
                                publishProgress("overview");
                            }
                            break;
                        case "albums":
                            if (!newAlbumsData.equals(albumsData)) {
                                publishProgress("albums");
                            }
                            break;
                        case "faces":
                            if (!newOverviewData.equals(overviewData)) {
                                publishProgress("faces");
                            }
                            break;
                        case "favorite":
                            if (!newFavoriteData.equals(favoriteData)) {
                                publishProgress("favorite");
                            }
                            break;
                    }
//                    wait(UPDATE_INTERVAL);
//                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            switch (text[0]) {
                case "overview":
                    overviewData = newOverviewData;
                    bundle.putStringArrayList("mediaList", overviewData);
                    overview.mainToFrag(bundle);
                    break;
                case "albums":
                    albumsData = newAlbumsData;
                    bundle.putStringArrayList("mediaList", albumsData);
                    albums.mainToFrag(bundle);
                    break;
                case "faces":
                    overviewData = newOverviewData;
                    bundle.putStringArrayList("mediaList", overviewData);
                    faces.mainToFrag(bundle);
                    break;
                case "favorite":
                    favoriteData = newFavoriteData;
                    bundle.putStringArrayList("mediaList", favoriteData);
                    favorite.mainToFrag(bundle);
                    break;
            }
            bundle.clear();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.isEmpty()) {
                Log.e(TAG, "mediaUpdater: Ended.");
            } else {
                Log.e(TAG, String.format("mediaUpdater: Ended due to exception '%s'.", result));
            }
        }
    }
}
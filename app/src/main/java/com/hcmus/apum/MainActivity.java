package com.hcmus.apum;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmus.apum.fragment.*;
import com.hcmus.apum.tool.MediaManager;

import java.util.*;

public class MainActivity extends AppCompatActivity implements MainCallbacks {

    // Static objects
    public static MediaManager mediaManager = new MediaManager();
    public static Boolean debugEnabled = true;

    // Request codes
    public static int
            PREVIEW_REQUEST_CODE = 97,
            CONTENT_REQUEST_CODE = 27,
            SEARCH_REQUEST_CODE = 5,
            CAMERA_REQUEST_CODE = 71,
            ABOUT_REQUEST_CODE = 46,
            COPY_CHOOSER_REQUEST_CODE = 77,
            MOVE_CHOOSER_REQUEST_CODE = 37;

    // Fragments
    public static final ArrayList<String> fragNames =
            new ArrayList<>(
                    Arrays.asList("overview", "albums", "faces", "favorite")
            );
    public static final ArrayList<Integer> fragIds =
            new ArrayList<>(
                    Arrays.asList(R.id.action_overview, R.id.action_albums, R.id.action_faces, R.id.action_favorite)
            );
    private static final ArrayList<BaseFragment> frags = new ArrayList<>();

    // For threads
    private static String currentFragment = fragNames.get(0);
    private static ArrayList<String> overviewData, albumsData, favoriteData,
            newOverviewData, newAlbumsData, newFavoriteData;
    private static int overviewSort, albumSort, favoriteSort;
    private static AsyncUpdater updater;
    private static boolean OVERRIDE_WAIT = false;

    // Debugging
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init data
        mediaManager.updateLocations(this);
        mediaManager.updateFavorite(this);
        // Sort codes
        overviewSort = mediaManager.SORT_BY_DATE + mediaManager.SORT_DESCENDING;
        albumSort = mediaManager.SORT_BY_NAME;
        favoriteSort = mediaManager.SORT_DEFAULT;
        // Fragments' mediaList
        overviewData = mediaManager.sort(mediaManager.getMedia(), overviewSort);
        albumsData = mediaManager.sort(mediaManager.getAlbums(), albumSort);
        favoriteData = mediaManager.sort(mediaManager.getFaces(), favoriteSort);
        // For auto-reloading mediaList
        newOverviewData = overviewData;
        newAlbumsData = albumsData;
        newFavoriteData = favoriteData;

        // Init fragments
        frags.add(OverviewFragment.newInstance(overviewData));
        frags.add(AlbumsFragment.newInstance(albumsData));
        frags.add(FacesFragment.newInstance(overviewData));
        frags.add(FavoriteFragment.newInstance(favoriteData));
        switchFragment(fragIds.get(0));

        // Init controls
        // GUI controls
        BottomNavigationView navBar = findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(item -> switchFragment(item.getItemId()));
        navBar.setOnNavigationItemReselectedListener(item -> scrollToTop(item.getItemId()));

        // Init updater
        updater = new AsyncUpdater();
        updater.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updater.isCancelled()) {
            updater.execute();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (updater.isCancelled()) {
            updater.execute();
        }
    }

    private boolean switchFragment(int itemId) {
        // Get id to switch fragment to
        int fragId = fragIds.indexOf(itemId);
        if (fragId < 0 || fragId > 3) {
            fragId = 0;
        }

        // Switch to fragment or scroll to top
        FragmentTransaction ft_navBar = getSupportFragmentManager().beginTransaction();
        ft_navBar.replace(R.id.frame, Objects.requireNonNull(frags.get(fragId)));
        currentFragment = fragNames.get(fragId);
        ft_navBar.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft_navBar.addToBackStack(null);
        ft_navBar.commit();

        // Reload on fragment load
        OVERRIDE_WAIT = true;
        return true;
    }

    private void switchFragment(String caller) {
        switchFragment(fragIds.get(fragNames.indexOf(caller)));
    }

    private void scrollToTop(int itemId) {
        // TODO: Scroll to top or reload based on scroll value
    }

    @Override
    public void fragToMain(String caller, Bundle bundle) {
        // Check action sent
        if (bundle.getString("action") != null) {
            String action = bundle.getString("action");
            switch (action) {
                case "sort":
                    // Forward sort bundle to fragments
                    if (fragNames.get(0).equals(caller)) {
                        overviewSort = bundle.getInt("sortCode");
                    } else if (fragNames.get(1).equals(caller)) {
                        albumSort = bundle.getInt("sortCode");
                    } else if (fragNames.get(3).equals(caller)) {
                        favoriteSort = bundle.getInt("sortCode");
                    }
                    OVERRIDE_WAIT = true;
                    break;
                case "reload":
                    OVERRIDE_WAIT = true;
                    break;
                case "switch":
                    switchFragment(bundle.getString("caller"));
                    break;
                default:
                    Log.w(TAG, "fragToMain received unknown action request: " + action);
            }
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
        private final static int UPDATE_INTERVAL = 10;

        @Override
        protected void onPreExecute() {
            bundle.putString("action", "reload");
        }

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        protected String doInBackground(String... params) {
            while (true) {
                try {
                    // Run every 10 secs
                    long now = new Date().toInstant().getEpochSecond();
                    if (now % UPDATE_INTERVAL != 0) {
                        if (!OVERRIDE_WAIT) {
                            continue;
                        }
                    }

                    Log.i(TAG, "doInBackground: Updating");
                    mediaManager.updateLocations(MainActivity.this);
                    mediaManager.updateFavorite(MainActivity.this);
                    newOverviewData = mediaManager.sort(mediaManager.getMedia(), overviewSort);
                    newAlbumsData = mediaManager.sort(mediaManager.getAlbums(), albumSort);
                    newFavoriteData = mediaManager.sort(mediaManager.getFavorite(), favoriteSort);
                    if (fragNames.get(0).equals(currentFragment)) {
                        if (!newOverviewData.equals(overviewData)) {
                            publishProgress(fragNames.get(0));
                        }
                    } else if (fragNames.get(1).equals(currentFragment)) {
                        if (!newAlbumsData.equals(albumsData)) {
                            publishProgress(fragNames.get(1));
                        }
                    } else if (fragNames.get(2).equals(currentFragment)) {
                        if (!newOverviewData.equals(overviewData)) {
                            publishProgress(fragNames.get(2));
                        }
                    } else if (fragNames.get(3).equals(currentFragment)) {
                        if (!newFavoriteData.equals(favoriteData)) {
                            publishProgress(fragNames.get(3));
                        }
                    }

                    // Notify on manual reload
                    if (OVERRIDE_WAIT) {
                        OVERRIDE_WAIT = false;
                        Toast.makeText(getApplicationContext(), getString(R.string.info_reload), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            Log.i(TAG, "onProgressUpdate: Found changes");
            int fragId = fragNames.indexOf(text[0]);
            if (fragNames.get(0).equals(text[0])) {
                overviewData = newOverviewData;
                bundle.putStringArrayList("mediaList", overviewData);
            } else if (fragNames.get(1).equals(text[0])) {
                albumsData = newAlbumsData;
                bundle.putStringArrayList("mediaList", albumsData);
            } else if (fragNames.get(2).equals(text[0])) {
                overviewData = newOverviewData;
                bundle.putStringArrayList("mediaList", overviewData);
            } else if (fragNames.get(3).equals(text[0])) {
                favoriteData = newFavoriteData;
                bundle.putStringArrayList("mediaList", favoriteData);
            }
            Objects.requireNonNull(frags.get(fragId)).mainToFrag(bundle);
            bundle.remove("mediaList");
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
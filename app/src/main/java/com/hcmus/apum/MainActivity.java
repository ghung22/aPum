package com.hcmus.apum;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmus.apum.fragment.AlbumsFragment;
import com.hcmus.apum.fragment.BaseFragment;
import com.hcmus.apum.fragment.FacesFragment;
import com.hcmus.apum.fragment.FavoriteFragment;
import com.hcmus.apum.fragment.OverviewFragment;
import com.hcmus.apum.tool.MediaManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MainCallbacks {
    // <editor-fold desc="INIT OBJECTS">
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

    // Fragments' data
    public static final ArrayList<String> fragNames =
            new ArrayList<>(Arrays.asList("overview", "albums", "faces", "favorite"));
    public static final ArrayList<Integer> fragIds =
            new ArrayList<>(Arrays.asList(R.id.action_overview, R.id.action_albums, R.id.action_faces, R.id.action_favorite));
    private static final ArrayList<BaseFragment> frags =
            new ArrayList<>(Arrays.asList(new BaseFragment[4]));
    private static final ArrayList<ArrayList<String>> fragData =
            new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    private static final ArrayList<ArrayList<String>> fragNewData =
            new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    private static final ArrayList<Integer> fragSorts =
            new ArrayList<>(Arrays.asList(new Integer[4]));

    // For threads
    private static String currentFragment = fragNames.get(0);
    private static AsyncUpdater updater;
    private static boolean OVERRIDE_WAIT = false;

    // Debugging
    private static final String TAG = "MainActivity";
    // </editor-fold>

    // <editor-fold desc="LIFECYCLE EVENTS">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init data
        mediaManager.updateLocations(this);
        mediaManager.updateFavorite(this);

        // Sort codes
        fragSorts.set(0, mediaManager.SORT_BY_DATE + mediaManager.SORT_DESCENDING);
        fragSorts.set(1, mediaManager.SORT_BY_NAME);
        fragSorts.set(2, mediaManager.SORT_DEFAULT);
        fragSorts.set(3, mediaManager.SORT_DEFAULT);

        // Fragment data - for reloading mediaList
        getNewData();
        fragData.set(0, fragNewData.get(0));
        fragData.set(1, fragNewData.get(1));
        fragData.set(2, fragNewData.get(3));
        fragData.set(3, fragNewData.get(2));

        // Fragments
        frags.set(0, OverviewFragment.newInstance(fragData.get(0)));
        frags.set(1, AlbumsFragment.newInstance(fragData.get(1)));
        frags.set(2, FacesFragment.newInstance(fragData.get(2)));
        frags.set(3, FavoriteFragment.newInstance(fragData.get(3)));
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
    // </editor-fold>

    // <editor-fold desc="PRIVATE METHODS">
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
        manualReload();
        return true;
    }

    private void switchFragment(String caller) {
        switchFragment(fragIds.get(fragNames.indexOf(caller)));
    }

    private void scrollToTop(int itemId) {
        Bundle bundle = new Bundle();
        bundle.putString("action", "scroll");
        frags.get(fragIds.indexOf(itemId)).mainToFrag(bundle);
    }

    private void manualReload() {
        OVERRIDE_WAIT = true;
    }

    private void getNewData() {
        fragNewData.set(0, mediaManager.sort(mediaManager.getMedia(), fragSorts.get(0)));
        fragNewData.set(1, mediaManager.sort(mediaManager.getAlbums(), fragSorts.get(1)));
        fragNewData.set(2, mediaManager.sort(mediaManager.getFaces(), fragSorts.get(2)));
        fragNewData.set(3, mediaManager.sort(mediaManager.getFavorite(), fragSorts.get(3)));
    }
    // </editor-fold>

    // <editor-fold desc="MISC EVENTS">
    @Override
    public void fragToMain(String caller, Bundle bundle) {
        // Check action sent
        if (bundle.getString("action") != null) {
            String action = bundle.getString("action");
            switch (action) {
                case "sort":
                    fragSorts.set(fragNames.indexOf(caller), bundle.getInt("sortCode"));
                    manualReload();
                    break;
                case "reload":
                    manualReload();
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
    // </editor-fold>

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
                    getNewData();
                    int fragId = fragNames.indexOf(currentFragment);
                    if (!fragNewData.get(fragId).equals(fragData.get(fragId))) {
                        publishProgress(currentFragment);
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
            fragData.set(fragId, fragNewData.get(fragId));
            bundle.putStringArrayList("mediaList", fragData.get(fragId));
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
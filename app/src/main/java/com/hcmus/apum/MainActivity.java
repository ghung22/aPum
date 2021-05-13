package com.hcmus.apum;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmus.apum.fragment.AlbumsFragment;
import com.hcmus.apum.fragment.FacesFragment;
import com.hcmus.apum.fragment.FavoriteFragment;
import com.hcmus.apum.fragment.OverviewFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

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
    private Fragment overview;
    private Fragment albums;
    private Fragment faces;
    private Fragment favorite;

    // For use/save state values
    private Bundle savedInstanceState;

    //Database
    DatabaseFavorites db_fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        // Init data
        mediaManager.updateLocations(this);
        mediaManager.updateFavoriteLocations(this);

        // Init fragments
        overview = OverviewFragment.newInstance(mediaManager.sort(mediaManager.getImages(), "date", false));
        albums = AlbumsFragment.newInstance(mediaManager.sort(mediaManager.getAlbums(), "name"));
        faces = FacesFragment.newInstance(mediaManager.sort(mediaManager.getImages(), "date", false));
        favorite = FavoriteFragment.newInstance(mediaManager.sort(mediaManager.getFaces(), "date", false));

        //Database
        db_fav = new DatabaseFavorites(this);
        try {
            db_fav.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        db_fav.openDataBase();
        // Init GUI
        FragmentTransaction ft_main = getSupportFragmentManager().beginTransaction();
        ft_main.replace(R.id.frame, overview);
        ft_main.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft_main.addToBackStack(null);
        ft_main.commit();

        // Init controls
        navBar = findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(item -> switchFragment(item.getItemId()));
    }
//    public void AddData(String newEntry){
//        insertData = db_fav.addData(newEntry);
//        if(insertData){
//            debugMsg("added successfully data");
//        }else{
//            debugMsg("Add Data false");
//        }
//    }
//    private void debugMsg(String msg){
//        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
//    }

    private boolean switchFragment(int itemId) {
        FragmentTransaction ft_navBar = getSupportFragmentManager().beginTransaction();
        switch (itemId) {
            case R.id.action_overview:
                ft_navBar.replace(R.id.frame, overview);
                break;
            case R.id.action_albums:
                ft_navBar.replace(R.id.frame, albums);
                break;
            case R.id.action_faces:
                ft_navBar.replace(R.id.frame, faces);
                break;
            case R.id.action_favorite:
                ft_navBar.replace(R.id.frame, favorite);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
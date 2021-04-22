package com.hcmus.apum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Static objects
    public static MediaManager mediaManager = new MediaManager();
    public static Boolean debugEnabled = true;

    // GUI controls
    private BottomNavigationView navBar;

    // Fragments
    private final Fragment overview = new OverviewFragment();
    private final Fragment albums = new AlbumsFragment();
    private final Fragment faces = new FacesFragment();
    private final Fragment favorite = new FavoriteFragment();

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
        // Init GUI
        FragmentTransaction ft_main = getSupportFragmentManager().beginTransaction();
        ft_main.replace(R.id.frame, overview);
        ft_main.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft_main.addToBackStack(null);
        ft_main.commit();

        // Init controls
        navBar = (BottomNavigationView) findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(item -> {
            FragmentTransaction ft_navBar = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
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
        });
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
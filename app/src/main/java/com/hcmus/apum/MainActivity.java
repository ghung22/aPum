package com.hcmus.apum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // GUI controls
    BottomNavigationView navBar;

    // Fragments
    Fragment overview = new OverviewFragment(),
            albums = new AlbumsFragment(),
            faces = new FacesFragment(),
            favorite = new FavoriteFragment();

    // For use/save state values
    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        // Init GUI
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame, overview);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

        // Init controls
        navBar = (BottomNavigationView) findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                String temp = (String) item.getTitle();
                if (temp == getString(R.string.action_overview)) {
                    ft.replace(R.id.frame, overview);
                } else if (temp == getString(R.string.action_albums)) {
                    ft.replace(R.id.frame, albums);
                } else if (temp == getString(R.string.action_faces)) {
                    ft.replace(R.id.frame, faces);
                } else if (temp == getString(R.string.action_favorite)) {
                    ft.replace(R.id.frame, favorite);
                }
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                return true;
            }
        });
    }
}
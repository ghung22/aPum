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
        navBar.setOnNavigationItemSelectedListener(item -> {
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.action_overview:
                    ft1.replace(R.id.frame, overview);
                    break;
                case R.id.action_albums:
                    ft1.replace(R.id.frame, albums);
                    break;
                case R.id.action_faces:
                    ft1.replace(R.id.frame, faces);
                    break;
                case R.id.action_favorite:
                    ft1.replace(R.id.frame, favorite);
                    break;
            }
            ft1.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft1.addToBackStack(null);
            ft1.commit();
            return true;
        });
    }
}
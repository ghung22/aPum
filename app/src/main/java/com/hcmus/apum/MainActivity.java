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
}
package com.example.btl_dnc.manager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.btl_dnc.R;
import com.example.btl_dnc.fragment.ManagerHomeFragment;
import com.example.btl_dnc.fragment.ManagerIncidentFragment;
import com.example.btl_dnc.fragment.ManagerNewsFragment;
import com.example.btl_dnc.fragment.ManagerProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManagerActivity extends AppCompatActivity {

    BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        nav = findViewById(R.id.bottomNavManager);

        // default
        loadFragment(new ManagerHomeFragment());

        nav.setOnItemSelectedListener(item -> {

            Fragment f;

            if (item.getItemId() == R.id.nav_home) {
                f = new ManagerHomeFragment();
            }
            else if (item.getItemId() == R.id.nav_incident) {
                f = new ManagerIncidentFragment();
            }
            else if (item.getItemId() == R.id.nav_news) {
                f = new ManagerNewsFragment();
            }
            else {
                f = new ManagerProfileFragment();
            }

            loadFragment(f);
            return true;
        });
    }

    void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameManager, f)
                .commit();
    }
}
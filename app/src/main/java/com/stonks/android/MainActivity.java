package com.stonks.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        final BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.home_nav:
                            // switch to HomeFragment
                            break;
                        case R.id.search_nav:
                            switchFragment(new SearchableFragment());
                            break;
                        case R.id.activity_nav:
                            // switch to ActivityFragment
                            break;
                        case R.id.settings_nav:
                            // switch to SettingsFragment
                            break;
                        default:
                            return false;
                    }

                    return true;
                });

        // disable the back button on the homepage
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}

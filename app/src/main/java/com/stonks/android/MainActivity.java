package com.stonks.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // home screen by default
        switchFragment(new HomePageFragment());

        final BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.home_nav:
                            switchFragment(new HomePageFragment());
                            break;
                        case R.id.search_nav:
                            switchFragment(new SearchableFragment());
                            break;
                        case R.id.activity_nav:
                            switchFragment(new RecentTransactionsFragment());
                            break;
                        case R.id.settings_nav:
                            switchFragment(new SettingsFragment());
                            break;
                        default:
                            return false;
                    }

                    return true;
                });

        // disable the back button on the homepage
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // initializing sliding drawer
        SlidingUpPanelLayout slidingUpPanel = findViewById(R.id.sliding_layout);
        slidingUpPanel.setPanelHeight(0);
        slidingUpPanel.setAnchorPoint(1.0f);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}

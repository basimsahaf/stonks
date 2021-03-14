package com.stonks.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // disable the back button on the homepage
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new SettingsParentFragment())
                .commit();

    }
}

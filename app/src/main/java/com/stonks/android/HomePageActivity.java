package com.stonks.android;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.stonks.android.adapter.HomePagerAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class HomePageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);



        final HomePagerAdapter adapter = new HomePagerAdapter(this, getSupportFragmentManager(), 2);

    }
}
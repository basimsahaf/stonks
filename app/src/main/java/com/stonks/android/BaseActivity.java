package com.stonks.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// sets up the default ActionBar appearance and actions
// all other activities should inherit from BaseActivity to inherit the ActionBar's default
// behaviour
// TODO: implement back button logic
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        super.onCreate(savedInstanceState);
    }
}

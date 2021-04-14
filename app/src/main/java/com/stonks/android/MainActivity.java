package com.stonks.android;

import android.app.DownloadManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.external.AlpacaWebSocket;
import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.utility.Formatters;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {
    private SlidingUpPanelLayout slidingUpPanel;
    private LinearLayout portfolioTitle;
    private TextView globalTitle;
    private AlpacaWebSocket socket;

    private long enqueue;
    private DownloadManager dm;

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
                        case R.id.saved_nav:
                            switchFragment(new SavedStocksFragment());
                            break;
                        case R.id.settings_nav:
                            switchFragment(new SettingsFragment());
                            break;
                        default:
                            return false;
                    }

                    return true;
                });

        LayoutInflater inflator =
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflator.inflate(R.layout.actionbar_layout, null);

        // Download company names json
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        firstStart = true; // testing

        if (firstStart) {
            Log.d("main activity", "about to enter job scheduer");
            ComponentName componentName = new ComponentName(this, StockJobScheduler.class);
            JobInfo info = new JobInfo.Builder(1, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(1440 * 60 * 1000)
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(info);

            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d("main activity", "job scheduled");
            }
            else {
                Log.d("main activity", "job scheduling failed");
            }
            Log.d("main activity", "first start is " + firstStart);
        }


        // each fragment can update these properties as needed
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(actionBarView);

        this.portfolioTitle =
                getSupportActionBar().getCustomView().findViewById(R.id.portfolio_title);
        this.globalTitle = getSupportActionBar().getCustomView().findViewById(R.id.global_title);

        // initializing sliding drawer
        slidingUpPanel = findViewById(R.id.sliding_layout);
        slidingUpPanel.setPanelHeight(0);
        slidingUpPanel.setAnchorPoint(1.0f);

        this.socket = new AlpacaWebSocket();
    }

    public void subscribe(String symbol, WebSocketObserver observer) {
        this.socket.subscribe(symbol, observer);
    }

    public void unsubscribe(String symbol) {
        this.socket.unsubscribe(symbol);
    }

    public void setPortfolioValue(float value) {
        globalTitle.setVisibility(View.GONE);
        portfolioTitle.setVisibility(View.VISIBLE);

        updatePortfolioValue(value);
    }

    public void updatePortfolioValue(float value) {
        TextView currentValue =
                getSupportActionBar().getCustomView().findViewById(R.id.current_value);
        currentValue.setText(Formatters.formatPrice(value));
    }

    public void setGlobalTitle(String title) {
        globalTitle.setText(title);

        portfolioTitle.setVisibility(View.GONE);
        globalTitle.setVisibility(View.VISIBLE);
    }

    public void setActionBarCustomViewAlpha(float alpha) {
        portfolioTitle.setAlpha(alpha);
    }

    public void hideActionBarCustomViews() {
        globalTitle.setVisibility(View.GONE);
        portfolioTitle.setVisibility(View.GONE);
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack(fragment.getClass().getCanonicalName()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public SlidingUpPanelLayout getSlidingUpPanel() {
        return slidingUpPanel;
    }
}

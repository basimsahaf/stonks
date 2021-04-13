package com.stonks.android;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.external.AlpacaWebSocket;
import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.utility.Formatters;

import java.io.File;
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

        Log.d("main activity", "first start is " + firstStart);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                            doesSymbolsExist();

                            Log.d("main activity", "what is URI strng " + uriString);
                            Uri a = Uri.parse(uriString);
                            File companyData = new File(a.getPath());
                            Log.d("main activity", "download finished and in local file now " + a.getPath());
                            // populate DB
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        if (firstStart) {
            downloadJSON();
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

    public void downloadJSON() {
        Log.d("main activity", "start json download at " + LocalDateTime.now());

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse("https://finnhub.io/api/v1/stock/symbol?exchange=US&token=c0krmsf48v6und6s0rig"))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "symbols.json");
        enqueue = dm.enqueue(request);

        Log.d("main activity", "finish json download at " + LocalDateTime.now());

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    private boolean doesSymbolsExist() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "symbols.json";
//        File file = new File(path);

        Log.d("main activitY", "check if symbols exists" + path);

        return false;
    }
}

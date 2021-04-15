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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.external.AlpacaWebSocket;
import com.stonks.android.external.CompanyDataManager;
import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.storage.CompanyTable;
import com.stonks.android.utility.Formatters;
import java.io.File;
import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {
    private SlidingUpPanelLayout slidingUpPanel;
    private LinearLayout portfolioTitle;
    private TextView globalTitle;
    private AlpacaWebSocket socket;

    private long enqueue;
    private DownloadManager dm;

    private static final String TAG = "MainActivity";

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
        BroadcastReceiver receiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                            long downloadID =
                                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                            DownloadManager.Query query = new DownloadManager.Query();
                            query.setFilterById(enqueue);
                            Cursor c = dm.query(query);
                            if (c.moveToFirst()) {
                                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                    String uriString =
                                            c.getString(
                                                    c.getColumnIndex(
                                                            DownloadManager.COLUMN_LOCAL_URI));

                                    Log.d(TAG, "intent recieved! finished downloading json");
                                    Uri a = Uri.parse(uriString);

                                    File companyData = new File(a.getPath());
                                    String location = companyData.getPath();

                                    Log.d(TAG, "start company data populating");
                                    CompanyTable ct = CompanyTable.getInstance(context);
                                    ct.emptyTable();
                                    CompanyTable.populateCompanyTableIfEmpty(
                                            getApplicationContext());
                                }
                            }
                        }
                    }
                };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            downloadStockData();

            // set up jobscheduler to download company data
            ComponentName companyDataSchedulerName = new ComponentName(this,
 StockJobDownloadScheduler.class);
            JobInfo companyDataSchedulerinfo = new JobInfo.Builder(1, companyDataSchedulerName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(1440 * 60 * 1000)
                    .build();
            JobScheduler companyDatascheduler = (JobScheduler)
 getSystemService(JOB_SCHEDULER_SERVICE);
            int companySchedulerResult = companyDatascheduler.schedule(companyDataSchedulerinfo);
            if (companySchedulerResult == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "download company info scheduled");
            }
            else {
                Log.d(TAG, "download company info job scheduling failed");
            }

            // set up jobscheduler to fill table
            ComponentName tableSchedulerName = new ComponentName(this,
                    StockJobDownloadScheduler.class);
            JobInfo tableSchedulerInfo = new JobInfo.Builder(2, tableSchedulerName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .setPeriodic(1440 * 60 * 1000)
                    .setMinimumLatency(60 * 1000)
                    .build();
            JobScheduler tableJobScheduler = (JobScheduler)
                    getSystemService(JOB_SCHEDULER_SERVICE);
            int tableSchedulerResult = tableJobScheduler.schedule(tableSchedulerInfo);
            if (tableSchedulerResult == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "download company info scheduled");
            }
            else {
                Log.d(TAG, "download company info job scheduling failed");
            }
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

    public void downloadStockData() {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "start json download at " + LocalDateTime.now());

                        if (CompanyDataManager.doesSymbolsFileExist()) {
                            CompanyDataManager.deletesSymbolsFile();
                        }
                        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request =
                                new DownloadManager.Request(
                                                Uri.parse(
                                                        CompanyDataManager.getFinnhubURL()))
                                        .setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                CompanyDataManager.FILE_NAME);
                        enqueue = dm.enqueue(request);
                        SharedPreferences prefs =
                                getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("firstStart", false);
                        editor.apply();
                    }
                })
        .start();
    }
}

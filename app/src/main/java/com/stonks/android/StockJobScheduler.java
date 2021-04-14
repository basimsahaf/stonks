package com.stonks.android;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class StockJobScheduler extends JobService {
    private static final String TAG = "StockJobScheduler";
    private DownloadManager dm;
    private long enqueue;
    Context context;
    Intent intent;

    StockJobScheduler(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        downloadStockData(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public void downloadStockData(JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                jobFinished(params, false);
            }
        }).start();
    }
}
package com.stonks.android;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.time.LocalDateTime;

public class StockJobScheduler extends JobService {
    private static final String TAG = "StockJobScheduler";
    private DownloadManager dm;
    private long enqueue;
    private BroadcastReceiver receiver;
    Context context;
    Intent intent;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        downloadStockData(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "job cancelled");
        return false;
    }

    public void downloadStockData(JobParameters params) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start json download at " + LocalDateTime.now());

                if(doesSymbolsFileExist()) {
                    deletesSymbolsFile();
                }
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse("https://finnhub.io/api/v1/stock/symbol?exchange=US&token=c0krmsf48v6und6s0rig"))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "symbols.json");
                enqueue = dm.enqueue(request);

                Log.d(TAG, "finish json download at " + LocalDateTime.now());

                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstStart", false);
                editor.apply();

                Log.d(TAG, "job finished");
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy called");

        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean doesSymbolsFileExist() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + Environment.DIRECTORY_DOWNLOADS + "/symbols.json";

        File file = new File(path);
        boolean exists = file.exists();
        Log.d("main activity", "does file exist " + exists);

        return exists;
    }

    public static boolean deletesSymbolsFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + Environment.DIRECTORY_DOWNLOADS + "/symbols.json";
        File file = new File(path);
        boolean deleted = file.delete();
        Log.d("main activity" , "file was deleted");
        return deleted;
    }
}
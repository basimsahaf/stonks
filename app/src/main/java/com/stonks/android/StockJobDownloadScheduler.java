package com.stonks.android;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.stonks.android.external.CompanyDataManager;
import java.time.LocalDateTime;

public class StockJobDownloadScheduler extends JobService {
    private static final String TAG = "StockJobScheduler";

    @Override
    public boolean onStartJob(JobParameters params) {
        downloadStockData(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public void downloadStockData(JobParameters params) {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    if (CompanyDataManager.doesSymbolsFileExist()) {
                        CompanyDataManager.deletesSymbolsFile();
                    }
                    DownloadManager dm =
                            (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request =
                            new DownloadManager.Request(
                                            Uri.parse(
                                                    CompanyDataManager.getFinnhubURL()))
                                    .setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            CompanyDataManager.FILE_NAME);
                    long enqueue = dm.enqueue(request);
                    jobFinished(params, false);
                }
            })
            .start();
    }
}

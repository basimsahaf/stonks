package com.stonks.android;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.Environment;

import com.stonks.android.external.CompanyDataManager;
import com.stonks.android.storage.CompanyTable;

public class companyTableUpdateScheduler extends JobService {
    private static final String TAG = "companyTableUpdateScheduler";

    @Override
    public boolean onStartJob(JobParameters params) {
        CompanyTable companyTable = CompanyTable.getInstance(getBaseContext());
        companyTable.emptyTable();
        companyTable.populateCompanyTableIfEmpty(getBaseContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}

package com.stonks.android.external;

import android.os.Environment;
import android.util.Log;
import com.stonks.android.BuildConfig;
import java.io.File;

public class CompanyDataManager {

    private static CompanyDataManager companyDataManager;
    public static String url =
            "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + BuildConfig.FINNHUB_TOKEN;
    public static final String FILE_NAME = "symbols.json";
    public static final String FILE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/"
                    + Environment.DIRECTORY_DOWNLOADS
                    + "/"
                    + FILE_NAME;

    private CompanyDataManager() {
        url = url + BuildConfig.FINNHUB_TOKEN;
    }

    public CompanyDataManager getInstance() {
        if (companyDataManager == null) {
            companyDataManager = new CompanyDataManager();
        }
        return companyDataManager;
    }

    public static String getFinnhubURL() {
        return url;
    }

    public static boolean deletesSymbolsFile() {
        File file = new File(FILE_PATH);
        boolean deleted = file.delete();
        Log.d("main activity", "file was deleted");
        return deleted;
    }

    public static boolean doesSymbolsFileExist() {
        File file = new File(FILE_PATH);
        boolean exists = file.exists();
        Log.d("main activity", "does file exist " + exists);

        return exists;
    }
}

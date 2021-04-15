package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.R;
import com.stonks.android.model.CompanyModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CompanyTable extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "COMPANY_TABLE";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_NAME = "name";
    private Resources mResources;
    private String TAG = CompanyTable.class.getCanonicalName();
    private static CompanyTable companyTable = null;
    public static String CREATE_STRING =
            "CREATE TABLE "
                    + TABLE_NAME
                    + " ("
                    + COLUMN_SYMBOL
                    + " TEXT, "
                    + COLUMN_NAME
                    + " TEXT, "
                    + " PRIMARY KEY ("
                    + COLUMN_SYMBOL
                    + "))";

    private CompanyTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, DatabaseHelper.TABLE_VERSION);
        mResources = context.getResources();
    }

    public static CompanyTable getInstance(Context context) {
        if (companyTable == null) {
            companyTable = new CompanyTable(context);
        }
        return companyTable;
    }

    public static void populateCompanyTableIfNotEmpty(Context context) {
        CompanyTable companyTable = CompanyTable.getInstance(context);
        if (companyTable.isEmpty()) {
            companyTable.populateTableInThread();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            DatabaseHelper.removeAllTables(db);
            DatabaseHelper.createAllTables(db);
        }
    }

    public Boolean addCompany(CompanyModel company) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SYMBOL, company.getSymbol());
        cv.put(COLUMN_NAME, company.getName());

        long insert = db.insert(TABLE_NAME, null, cv);
        return insert >= 0;
    }

    public String getCompanyName(String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                String.format(
                        "SELECT %s FROM %s WHERE %s = ?", COLUMN_NAME, TABLE_NAME, COLUMN_SYMBOL);
        Cursor cursor = db.rawQuery(query, new String[] {symbol});

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        }
        return "";
    }

    public ArrayList<CompanyModel> getMatchingCompanies(String match) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT * FROM "
                        + TABLE_NAME
                        + " WHERE "
                        + COLUMN_SYMBOL
                        + " LIKE \"%"
                        + match
                        + "%\" OR "
                        + COLUMN_NAME
                        + " LIKE \"%"
                        + match
                        + "%\"";
        Cursor cursor = db.rawQuery(query, new String[] {});

        ArrayList<CompanyModel> companies = new ArrayList<>();
        int symbolColumnIndex = cursor.getColumnIndex(COLUMN_SYMBOL);
        int nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME);

        if (cursor.moveToFirst()) {
            do {
                String symbol = cursor.getString(symbolColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                companies.add(new CompanyModel(symbol, name));
            } while (cursor.moveToNext());
        }
        return companies;
    }

    private void populateTableInThread() {
        new Thread(this::populateTable).start();
    }

    private void populateTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String SYMBOL = "symbol";
        String NAME = "description";
        String TYPE = "type";

        try {
            String jsonDataString = readJsonDataFromFile();
            JSONArray companyJsonArray = new JSONArray(jsonDataString);
            db.beginTransaction();

            for (int i = 0; i < companyJsonArray.length(); ++i) {

                JSONObject item = companyJsonArray.getJSONObject(i);
                String symbol = item.getString(SYMBOL);
                String name = item.getString(NAME);
                String type = item.getString(TYPE);

                if (type.equalsIgnoreCase("common stock")) {
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_SYMBOL, symbol);
                    cv.put(COLUMN_NAME, name);
                    db.insert(TABLE_NAME, null, cv);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Boolean isEmpty() {
        String query = String.format("SELECT COUNT(*) FROM %s", TABLE_NAME);
        String COUNT = "COUNT(*)";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] {});
        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex(COUNT));
        }
        return count == 0;
    }

    private String readJsonDataFromFile() {

        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();

        try {
            String jsonDataString = null;
            inputStream = mResources.openRawResource(R.raw.symbols);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((jsonDataString = bufferedReader.readLine()) != null) {
                builder.append(jsonDataString);
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
        return new String(builder);
    }
}

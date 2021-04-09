package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.R;
import com.stonks.android.model.LoggedInUser;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;
import java.time.LocalDateTime;

public class UserTable extends SQLiteOpenHelper {
    private static UserTable userTable;
    private final String TAG = UserTable.class.getCanonicalName();
    private final float INITIAL_AMOUNT = 100000f;

    public static final String TABLE_NAME = "USER_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_BIOMETRICS = "biometrics_enabled";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_TRAINING_START_DATE = "training_start_date";
    public static final String CREATE_STRING =
            "CREATE TABLE "
                    + TABLE_NAME
                    + " ("
                    + COLUMN_USERNAME
                    + " TEXT PRIMARY KEY, "
                    + COLUMN_PASSWORD
                    + " TEXT,"
                    + COLUMN_BIOMETRICS
                    + " INT,"
                    + COLUMN_TOTAL_AMOUNT
                    + " REAL,"
                    + COLUMN_TRAINING_START_DATE
                    + " STRING"
                    + ")";

    private UserTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 6);
    }

    public static UserTable getInstance(Context context) {
        if (userTable == null) {
            userTable = new UserTable(context);
        }

        return userTable;
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

    public boolean addUser(UserModel userModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, userModel.getUsername());
        cv.put(COLUMN_PASSWORD, userModel.getPassword());
        cv.put(COLUMN_BIOMETRICS, userModel.getBiometricsEnabled());
        cv.put(COLUMN_TOTAL_AMOUNT, INITIAL_AMOUNT);
        cv.put(COLUMN_TRAINING_START_DATE, LocalDateTime.now().toString());

        long insert = db.insert(TABLE_NAME, null, cv);
        return insert >= 0;
    }

    public boolean checkIfUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";
        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUsernamePassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                "SELECT * FROM "
                        + TABLE_NAME
                        + " WHERE username = '"
                        + username
                        + "' AND password = '"
                        + password
                        + "'";

        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public Result<LoggedInUser> getBiometricsUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                String.format("SELECT * FROM %s WHERE %s = 1", TABLE_NAME, COLUMN_BIOMETRICS);
        Cursor cursor = db.rawQuery(query, null);
        LoggedInUser loggedInUser;
        if (cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            loggedInUser = new LoggedInUser(username);
            return new Result.Success<>(loggedInUser);
        }
        return new Result.Error(R.string.no_biometrics);
    }

    public float getFunds(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";

        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();

        if (exists) {
            float funds = cursor.getFloat(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));
            cursor.close();

            return funds;
        }

        cursor.close();
        return -1.0f;
    }

    public boolean updateFunds(String username, float newFunds) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = String.format(COLUMN_USERNAME + " = ? ");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_TOTAL_AMOUNT, newFunds);

        long update = db.update(TABLE_NAME, cv, whereClause, new String[] {username});
        return update >= 0;
    }

    public LocalDateTime getTrainingStartDate(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";

        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();

        if (exists) {
            String date = cursor.getString(cursor.getColumnIndex(COLUMN_TRAINING_START_DATE));
            cursor.close();

            return LocalDateTime.parse(date);
        }

        cursor.close();
        return null;
    }

    public boolean updateTrainingStartDate(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = String.format(COLUMN_USERNAME + " = ? ");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_TRAINING_START_DATE, LocalDateTime.now().toString());

        long update = db.update(TABLE_NAME, cv, whereClause, new String[] {username});
        return update >= 0;
    }
}

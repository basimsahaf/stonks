package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.R;
import com.stonks.android.model.LoggedInUser;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;

public class UserTable extends SQLiteOpenHelper {
    private final String TAG = UserTable.class.getCanonicalName();
    private final float INITIAL_AMOUNT = 100000f;

    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_BIOMETRICS = "biometrics_enabled";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";

    public UserTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable =
                "CREATE TABLE "
                        + USER_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT PRIMARY KEY, "
                        + COLUMN_PASSWORD
                        + " TEXT,"
                        + COLUMN_BIOMETRICS
                        + " INT,"
                        + COLUMN_TOTAL_AMOUNT
                        + " REAL"
                        + ")";

        db.execSQL(createUserTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropStatement = "DROP TABLE IF EXISTS " + USER_TABLE;
        db.execSQL(dropStatement);
        onCreate(db);
    }

    public boolean addUser(UserModel userModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, userModel.getUsername());
        cv.put(COLUMN_PASSWORD, userModel.getPassword());
        cv.put(COLUMN_BIOMETRICS, userModel.getBiometricsEnabled());
        cv.put(COLUMN_TOTAL_AMOUNT, INITIAL_AMOUNT);

        long insert = db.insert(USER_TABLE, null, cv);
        return insert >= 0;
    }

    public boolean checkIfUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + USER_TABLE + " WHERE username = '" + username + "'";
        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUsernamePassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                "SELECT * FROM "
                        + USER_TABLE
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

    public Result<LoggedInUser> changeUsername(String oldUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'",
                        USER_TABLE, COLUMN_USERNAME, oldUsername);
        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, oldUsername);

        if (cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String biometrics = cursor.getString(cursor.getColumnIndex(COLUMN_BIOMETRICS));
            String totalAmount = cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, newUsername);
            cv.put(COLUMN_PASSWORD, password);
            cv.put(COLUMN_BIOMETRICS, biometrics);
            cv.put(COLUMN_TOTAL_AMOUNT, totalAmount);

            try {
                db.update(USER_TABLE, cv, whereClause, null);
                cursor.close();
                return new Result.Success<>(new LoggedInUser(newUsername));
            } catch (SQLiteConstraintException e) {
                return new Result.Error(R.string.user_exists);
            }
        }
        // this shouldn't happen but just in case something goes wrong, this will allow graceful
        // exit
        return new Result.Error(R.string.internal_server_error);
    }

    public boolean verifyCurrentPassword(String currentUsername, String currentPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'",
                        USER_TABLE,
                        COLUMN_USERNAME,
                        currentUsername,
                        COLUMN_PASSWORD,
                        currentPassword);
        Cursor cursor = db.rawQuery(query, null);
        boolean result = false;
        if (cursor.moveToFirst()) {
            result = true;
        }
        cursor.close();
        return result;
    }

    public Result<LoggedInUser> changePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", USER_TABLE, COLUMN_USERNAME, username);
        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, username);

        if (cursor.moveToFirst()) {
            String biometrics = cursor.getString(cursor.getColumnIndex(COLUMN_BIOMETRICS));
            String totalAmount = cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, username);
            cv.put(COLUMN_PASSWORD, newPassword);
            cv.put(COLUMN_BIOMETRICS, biometrics);
            cv.put(COLUMN_TOTAL_AMOUNT, totalAmount);

            try {
                db.update(USER_TABLE, cv, whereClause, null);
                cursor.close();
                return new Result.Success<>(new LoggedInUser(username));
            } catch (SQLiteConstraintException e) {
                return new Result.Error(R.string.password_update_error);
            }
        }
        // this shouldn't happen but just in case something goes wrong, this will allow graceful
        // exit
        return new Result.Error(R.string.internal_server_error);
    }
}

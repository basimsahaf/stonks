package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
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

    public float getTotalAmountAvailable(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";

        Cursor cursor = db.rawQuery(queryString, null);
        float amountAvailableToTrade = 0f;

        if (cursor.moveToFirst()) {
            amountAvailableToTrade =
                    Float.parseFloat(cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT)));
        }

        return amountAvailableToTrade;
    }

    // TODO: once UserManager is written,this function can be refactored
    public boolean updateTotalAmount(String username, float newAmount) {

        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);

        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, username);

        if (cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String biometrics = cursor.getString(cursor.getColumnIndex(COLUMN_BIOMETRICS));
            String totalAmount = cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, username);
            cv.put(COLUMN_PASSWORD, password);
            cv.put(COLUMN_BIOMETRICS, biometrics);
            cv.put(COLUMN_TOTAL_AMOUNT, newAmount);

            try {
                db.update(TABLE_NAME, cv, whereClause, null);
                cursor.close();
                return true;
            } catch (SQLiteConstraintException e) {
                Log.d(TAG, e.toString());
            }
        }
        return false;
    }

    public Result<LoggedInUser> changeUsername(String oldUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'",
                        TABLE_NAME, COLUMN_USERNAME, oldUsername);
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
                db.update(TABLE_NAME, cv, whereClause, null);
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
                        TABLE_NAME,
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

    public Result<LoggedInUser> toggleBiometrics(String username, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, username);

        if (cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String totalAmount = cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, username);
            cv.put(COLUMN_PASSWORD, password);
            cv.put(COLUMN_BIOMETRICS, status);
            cv.put(COLUMN_TOTAL_AMOUNT, totalAmount);

            Log.d("Usertable", "trying to disable biometrics");

            try {
                db.update(TABLE_NAME, cv, whereClause, null);
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

    public Result<LoggedInUser> changePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
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
                db.update(TABLE_NAME, cv, whereClause, null);
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

    public Result<LoggedInUser> changeTrainingAmount(String username, float amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, username);

        if (cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String biometrics = cursor.getString(cursor.getColumnIndex(COLUMN_BIOMETRICS));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, username);
            cv.put(COLUMN_PASSWORD, password);
            cv.put(COLUMN_BIOMETRICS, biometrics);
<<<<<<< HEAD
            cv.put(COLUMN_TOTAL_AMOUNT, newAmount);

            try {
                db.update(USER_TABLE, cv, whereClause, null);
                cursor.close();
                return true;
            } catch (SQLiteConstraintException e) {
                Log.d(TAG, e.toString());
            }
        }
        return false;
=======
            cv.put(COLUMN_TOTAL_AMOUNT, amount);

            try {
                db.update(TABLE_NAME, cv, whereClause, null);
                cursor.close();
                return new Result.Success<>(new LoggedInUser(username));
            } catch (SQLiteConstraintException e) {
                return new Result.Error(R.string.training_period_error);
            }
        }
        // this shouldn't happen but just in case something goes wrong, this will allow graceful
        // exit
        return new Result.Error(R.string.internal_server_error);
>>>>>>> main
    }
}

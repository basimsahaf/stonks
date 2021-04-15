package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.R;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;
import java.time.LocalDateTime;

public class UserTable extends SQLiteOpenHelper {
    private static UserTable userTable;
    private final String TAG = UserTable.class.getCanonicalName();
    private final float INITIAL_AMOUNT = 100000f;
    private final int DEFAULT_BIOMETRICS = 0;

    public static final String TABLE_NAME = "USER_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_BIOMETRICS = "biometrics_enabled";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_TRAINING_START_DATE = "training_start_date";
    public static final String CREATE_STRING =
            "CREATE TABLE IF NOT EXISTS "
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
        super(context, BuildConfig.DATABASE_NAME, null, DatabaseHelper.TABLE_VERSION);
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
        DatabaseHelper.createAllTables(db);
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
        cv.put(COLUMN_BIOMETRICS, userModel.isBiometricsEnabled());
        cv.put(COLUMN_TOTAL_AMOUNT, INITIAL_AMOUNT);
        cv.put(COLUMN_TRAINING_START_DATE, LocalDateTime.now().toString());

        long insert = db.insert(TABLE_NAME, null, cv);
        return insert >= 0;
    }

    public Result<UserModel> login(String username, String password) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);

        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            if (userModel.getPassword().equals(password)) {
                return new Result.Success<UserModel>(userModel);
            }
            return new Result.Error(R.string.invalid_password);
        }
        return new Result.Error(R.string.user_does_not_exist);
    }

    public Result<UserModel> signUp(String username, String password) {
        UserModel newUser =
                new UserModel(
                        username,
                        password,
                        DEFAULT_BIOMETRICS,
                        INITIAL_AMOUNT,
                        LocalDateTime.now().toString());
        if (addUser(newUser)) {
            return new Result.Success<UserModel>(newUser);
        }
        return new Result.Error(R.string.signup_failed);
    }

    public Result<UserModel> changeUsername(String oldUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'",
                        TABLE_NAME, COLUMN_USERNAME, oldUsername);
        Cursor cursor = db.rawQuery(query, null);
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, oldUsername);

        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            userModel.setUsername(newUsername);
            if (updateUser(userModel, oldUsername)) {
                return new Result.Success<UserModel>(userModel);
            }
            return new Result.Error(R.string.user_exists);
        }
        // this shouldn't happen but just in case something goes wrong, this will
        // allow graceful exit
        return new Result.Error(R.string.internal_server_error);
    }

    public Result<UserModel> changePassword(String username, String newPassword) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            userModel.setPassword(newPassword);
            if (updateUser(userModel, username)) {
                return new Result.Success<UserModel>(userModel);
            }

            return new Result.Error(R.string.password_update_error);
        }
        // this shouldn't happen but just in case something goes wrong, this will
        // allow graceful exit
        return new Result.Error(R.string.internal_server_error);
    }

    public Result<UserModel> toggleBiometrics(String username, boolean status) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            userModel.setBiometricsEnabled(status ? 1 : 0);
            if (updateUser(userModel, username)) {
                return new Result.Success<UserModel>(userModel);
            }
            return new Result.Error(R.string.biometrics_in_use);
        }
        // this shouldn't happen but just in case something goes wrong, this will
        // allow graceful exit
        return new Result.Error(R.string.internal_server_error);
    }

    public Result<UserModel> getBiometricsUser() {
        String query =
                String.format("SELECT * FROM %s WHERE %s = 1", TABLE_NAME, COLUMN_BIOMETRICS);
        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            return new Result.Success<UserModel>(userModel);
        }
        return new Result.Error(R.string.internal_server_error);
    }

    public Result<UserModel> changeTrainingAmount(String username, float amount) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, COLUMN_USERNAME, username);
        UserModel userModel = getUserModel(query);

        if (userModel != null) {
            // need to update the date as well
            userModel.setTrainingAmount(amount);
            userModel.setTrainingStartDate(LocalDateTime.now().toString());
            if (updateUser(userModel, username)) {
                return new Result.Success<UserModel>(userModel);
            }

            return new Result.Error(R.string.training_period_error);
        }
        // this shouldn't happen but just in case something goes wrong, this will
        // allow graceful exit
        return new Result.Error(R.string.internal_server_error);
    }

    public LocalDateTime getTrainingStartDate(String username) {
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";
        UserModel userModel = getUserModel(queryString);

        if (userModel != null) {
            return LocalDateTime.parse(userModel.getTrainingStartDate());
        }
        return null;
    }

    public float getFunds(String username) {
        String queryString = "SELECT * FROM " + TABLE_NAME + " WHERE username = '" + username + "'";
        UserModel userModel = getUserModel(queryString);
        if (userModel != null) {
            return userModel.getTrainingAmount();
        }
        return -1.0f;
    }

    private UserModel getUserModel(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        UserModel userModel = null;

        if (cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String biometrics = cursor.getString(cursor.getColumnIndex(COLUMN_BIOMETRICS));
            String totalAmount = cursor.getString(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT));
            String startDate = cursor.getString(cursor.getColumnIndex(COLUMN_TRAINING_START_DATE));
            userModel =
                    new UserModel(
                            username,
                            password,
                            Integer.parseInt(biometrics),
                            Float.parseFloat(totalAmount),
                            startDate);
        }
        cursor.close();
        return userModel;
    }

    private boolean updateUser(UserModel userModel, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = String.format("%s = '%s'", COLUMN_USERNAME, username);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERNAME, userModel.getUsername());
        contentValues.put(COLUMN_PASSWORD, userModel.getPassword());
        contentValues.put(COLUMN_BIOMETRICS, userModel.isBiometricsEnabled());
        contentValues.put(COLUMN_TOTAL_AMOUNT, userModel.getTrainingAmount());
        contentValues.put(COLUMN_TRAINING_START_DATE, userModel.getTrainingStartDate());

        try {
            db.update(TABLE_NAME, contentValues, whereClause, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

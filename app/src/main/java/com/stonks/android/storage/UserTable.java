package com.stonks.android.storage.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.stonks.android.model.UserModel;

public class UserTable extends SQLiteOpenHelper {
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_PASSWORD = "password";

    public UserTable(@Nullable Context context) {
        super(context, "stonks_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAuthTable =
                "CREATE TABLE "
                        + USER_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT PRIMARY KEY, "
                        + COLUMN_PASSWORD
                        + " TEXT"
                        + ")";

        db.execSQL(createAuthTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropStatement = "DROP TABLE stonks_db.USER_TABLE";
        db.execSQL(dropStatement);
        onCreate(db);
    }

    public boolean addUser(UserModel userModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, userModel.getUsername());
        cv.put(COLUMN_PASSWORD, userModel.getPassword());

        long insert = db.insert(USER_TABLE, null, cv);
        return insert >= 0;
    }

    public boolean checkIfUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                "SELECT * FROM " + USER_TABLE + " WHERE AUTH_TABLE.username = '" + username + "'";
        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        Log.d("Debug", "User exist " + exists);
        return exists;
    }

    public boolean checkUsernamePassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                "SELECT * FROM "
                        + USER_TABLE
                        + " WHERE AUTH_TABLE.username = '"
                        + username
                        + "' AND AUTH_TABLE.password = '"
                        + password
                        + "'";
        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        Log.d("Debug", "Password exist " + exists);

        return exists;
    }
}

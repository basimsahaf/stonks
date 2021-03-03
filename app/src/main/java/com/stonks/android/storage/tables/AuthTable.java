package com.stonks.android.storage.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.stonks.android.models.UserModel;

public class AuthTable extends SQLiteOpenHelper {
    public static final String AUTH_TABLE = "AUTH_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_PASSWORD = "password";

    public AuthTable(@Nullable Context context) {
        super(context, "stonks_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAuthTable =
                "CREATE TABLE " + AUTH_TABLE + " (" +
                        COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                        COLUMN_FIRST_NAME + " TEXT, " +
                        COLUMN_LAST_NAME + " TEXT, " +
                        COLUMN_PASSWORD + " TEXT" +
                        ")";

        db.execSQL(createAuthTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropStatement = "DROP TABLE stonks_db.AUTH_TABLE";
        db.execSQL(dropStatement);
        onCreate(db);
    }

    public boolean addUser(UserModel userModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, userModel.getUsername());
        cv.put(COLUMN_FIRST_NAME, userModel.getFirstName());
        cv.put(COLUMN_LAST_NAME, userModel.getLastName());
        cv.put(COLUMN_PASSWORD, userModel.getPassword());

        long insert = db.insert(AUTH_TABLE, null, cv);
        return insert >= 0;
    }

    public boolean checkIfUserExists(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "SELECT * FROM " + AUTH_TABLE + " WHERE AUTH_TABLE.username = \"" + username + "\"";
        Cursor cursor = db.rawQuery(queryString, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
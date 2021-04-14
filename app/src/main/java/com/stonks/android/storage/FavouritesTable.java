package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.model.FavouriteStock;
import java.util.ArrayList;

public class FavouritesTable extends SQLiteOpenHelper {
    private static FavouritesTable favouritesTable;

    public static final String TABLE_NAME = "FAVOURITES_TABLE";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String CREATE_STRING =
            "CREATE TABLE "
                    + TABLE_NAME
                    + " ("
                    + COLUMN_USERNAME
                    + " TEXT, "
                    + COLUMN_SYMBOL
                    + " TEXT, "
                    + " FOREIGN KEY ( "
                    + COLUMN_USERNAME
                    + " ) "
                    + " REFERENCES "
                    + UserTable.TABLE_NAME
                    + " ( "
                    + UserTable.COLUMN_USERNAME
                    + " )"
                    + ")";

    private FavouritesTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, DatabaseHelper.TABLE_VERSION);
    }

    public static FavouritesTable getInstance(Context context) {
        if (favouritesTable == null) {
            favouritesTable = new FavouritesTable(context);
        }

        return favouritesTable;
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

    public boolean addFavouritesRow(FavouriteStock favourite) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, favourite.getUsername());
        cv.put(COLUMN_SYMBOL, favourite.getSymbol());

        long insert = db.insert(TABLE_NAME, null, cv);
        return insert >= 0;
    }

    public boolean deleteFavouritesRow(String username, String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        String whereArgs = String.format("%s = ? AND %s = ?", COLUMN_USERNAME, COLUMN_SYMBOL);

        return db.delete(TABLE_NAME, whereArgs, new String[] {username, symbol}) > 0;
    }

    public ArrayList<FavouriteStock> getUserFavourites(String username) {
        String query = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COLUMN_USERNAME);

        return queryFavouritesRows(query, new String[] {username});
    }

    public Boolean doesFavouriteStockExist(String username, String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %S = ?",
                        TABLE_NAME, COLUMN_USERNAME, COLUMN_SYMBOL);
        Cursor cursor = db.rawQuery(queryString, new String[] {username, symbol});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private ArrayList<FavouriteStock> queryFavouritesRows(String query, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<FavouriteStock> favouriteStocksList = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));

                favouriteStocksList.add(new FavouriteStock(username, symbol));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return favouriteStocksList;
    }
}

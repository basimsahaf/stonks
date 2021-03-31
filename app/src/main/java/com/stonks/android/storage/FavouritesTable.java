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
    public static final String FAVOURITES_TABLE = "FAVOURITES_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_CREATED_AT = "created_at";

    public FavouritesTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFavouritesTable =
                "CREATE TABLE "
                        + FAVOURITES_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT, "
                        + COLUMN_SYMBOL
                        + " TEXT, "
                        + COLUMN_CREATED_AT
                        + " TEXT, "
                        + " FOREIGN KEY ( "
                        + COLUMN_USERNAME
                        + " ) "
                        + " REFERENCES "
                        + UserTable.USER_TABLE
                        + " ( "
                        + UserTable.COLUMN_USERNAME
                        + " )"
                        + ")";

        db.execSQL(createFavouritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            String dropStatement = "DROP TABLE IF EXISTS stonks." + FAVOURITES_TABLE;
            db.execSQL(dropStatement);
            onCreate(db);
        }
    }

    public boolean addFavouritesRow(FavouriteStock favourite) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, favourite.getUsername());
        cv.put(COLUMN_SYMBOL, favourite.getSymbol());
        cv.put(COLUMN_CREATED_AT, favourite.getCreatedAt());

        long insert = db.insert(FAVOURITES_TABLE, null, cv);
        return insert >= 0;
    }

    public boolean deleteFavouritesRow(String username, String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        String whereArgs = String.format("%s = ? AND %s = ?", COLUMN_USERNAME, COLUMN_SYMBOL);

        return db.delete(FAVOURITES_TABLE, whereArgs, new String[] {username, symbol}) > 0;
    }

    public ArrayList<FavouriteStock> getUserFavourites(String username) {
        String query =
                String.format("SELECT * FROM %s WHERE %s = ?", FAVOURITES_TABLE, COLUMN_USERNAME);

        return queryFavouritesRows(query, new String[] {username});
    }

    private ArrayList<FavouriteStock> queryFavouritesRows(String query, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<FavouriteStock> favouriteStocksList = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
                String createdAt = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_AT));

                favouriteStocksList.add(new FavouriteStock(username, symbol, createdAt));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return favouriteStocksList;
    }
}

package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.model.PortfolioItem;
import java.util.ArrayList;

public class PortfolioTable extends SQLiteOpenHelper {
    private static PortfolioTable portfolioTable;

    public static final String PORTFOLIO_TABLE = "PORTFOLIO_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_SYMBOL = "symbol";

    private PortfolioTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 2);
    }

    public static PortfolioTable getInstance(Context context) {
        if (portfolioTable == null) {
            portfolioTable = new PortfolioTable(context);
        }

        return portfolioTable;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPortfolioTable =
                "CREATE TABLE "
                        + PORTFOLIO_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT, "
                        + COLUMN_QUANTITY
                        + " INTEGER, "
                        + COLUMN_SYMBOL
                        + " TEXT, "
                        + " PRIMARY KEY ( "
                        + COLUMN_USERNAME
                        + ", "
                        + COLUMN_SYMBOL
                        + "), "
                        + "FOREIGN KEY("
                        + COLUMN_USERNAME
                        + ") REFERENCES "
                        + UserTable.USER_TABLE
                        + "("
                        + UserTable.COLUMN_USERNAME
                        + "))";
        ;

        db.execSQL(createPortfolioTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            String dropStatement = "DROP TABLE IF EXISTS " + PORTFOLIO_TABLE;
            db.execSQL(dropStatement);
            onCreate(db);
        }
    }

    public boolean addPortfolioItem(PortfolioItem portfolioItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, portfolioItem.getUsername());
        cv.put(COLUMN_QUANTITY, portfolioItem.getQuantity());
        cv.put(COLUMN_SYMBOL, portfolioItem.getSymbol());

        long insert = db.insert(PORTFOLIO_TABLE, null, cv);
        return insert >= 0;
    }

    public boolean updatePortfolioItem(PortfolioItem portfolioItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = String.format(COLUMN_USERNAME + " = ? AND " + COLUMN_SYMBOL + " = ?");
        ContentValues cv = new ContentValues();
        String username = portfolioItem.getUsername();
        String symbol = portfolioItem.getSymbol();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_QUANTITY, portfolioItem.getQuantity());
        cv.put(COLUMN_SYMBOL, symbol);

        long update = db.update(PORTFOLIO_TABLE, cv, whereClause, new String[] {username, symbol});
        return update >= 0;
    }

    public boolean deletePortfolioItem(String username, String symbol) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = String.format(COLUMN_USERNAME + " = ? AND " + COLUMN_SYMBOL + " = ?");

        long delete = db.delete(PORTFOLIO_TABLE, whereClause, new String[] {username, symbol});
        return delete >= 0;
    }

    public boolean checkIfPortfolioItemExists(String username, String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %S = ?",
                        PORTFOLIO_TABLE, COLUMN_USERNAME, COLUMN_SYMBOL);
        Cursor cursor = db.rawQuery(queryString, new String[] {username, symbol});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public ArrayList<PortfolioItem> getPortfolioItems(String username) {
        String query =
                String.format("SELECT * FROM %s WHERE %s = ?", PORTFOLIO_TABLE, COLUMN_USERNAME);

        return queryPortfolioItems(query, new String[] {username});
    }

    public ArrayList<PortfolioItem> getPortfolioItemsBySymbol(String username, String symbol) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %s = ?",
                        PORTFOLIO_TABLE, COLUMN_USERNAME, COLUMN_SYMBOL);
        return queryPortfolioItems(query, new String[] {username, symbol});
    }

    private ArrayList<PortfolioItem> queryPortfolioItems(String query, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<PortfolioItem> portfolioItems = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
                int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                portfolioItems.add(new PortfolioItem(username, symbol, quantity));

            } while (cursor.moveToNext());
        }

        return portfolioItems;
    }
}

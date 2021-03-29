package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.model.TransactionRow;
import java.util.ArrayList;

public class TransactionTable extends SQLiteOpenHelper {
    public static final String TRANSACTION_TABLE = "TRANSACTION_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";

    public TransactionTable(@Nullable Context context) {
        super(context, "stonks_db2", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPortfolioTable =
                "CREATE TABLE "
                        + TRANSACTION_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT, "
                        + COLUMN_QUANTITY
                        + " INTEGER, "
                        + COLUMN_SYMBOL
                        + " TEXT, "
                        + COLUMN_TRANSACTION_TYPE
                        + " TEXT, "
                        + COLUMN_PRICE
                        + " REAL"
                        + ")";

        db.execSQL(createPortfolioTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            String dropStatement = "DROP TABLE IF EXISTS " + TRANSACTION_TABLE;
            db.execSQL(dropStatement);
            onCreate(db);
        }
    }

    public boolean addTransactionRow(TransactionRow transactionRow) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, transactionRow.getUsername());
        cv.put(COLUMN_QUANTITY, transactionRow.getQuantity());
        cv.put(COLUMN_SYMBOL, transactionRow.getSymbol());
        cv.put(COLUMN_PRICE, transactionRow.getPrice());
        cv.put(COLUMN_TRANSACTION_TYPE, transactionRow.getTransactionType());

        long insert = db.insert(TRANSACTION_TABLE, null, cv);
        return insert >= 0;
    }

    public ArrayList<TransactionRow> getTransactionRows(String username) {
        String query =
                String.format("SELECT * FROM %s WHERE %s = ?", TRANSACTION_TABLE, COLUMN_USERNAME);

        return queryTransactionRows(query, new String[] {username});
    }

    public ArrayList<TransactionRow> getTransactionRowsBySymbol(String username, String symbol) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %s = ?",
                        TRANSACTION_TABLE, COLUMN_USERNAME, COLUMN_SYMBOL);
        return queryTransactionRows(query, new String[] {username, symbol});
    }

    private ArrayList<TransactionRow> queryTransactionRows(String query, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TransactionRow> transactionRows = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
                int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                float price = cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE));
                String transactionType =
                        cursor.getString(cursor.getColumnIndex(COLUMN_TRANSACTION_TYPE));
                transactionRows.add(
                        new TransactionRow(username, symbol, quantity, price, transactionType));

            } while (cursor.moveToNext());
        }
        return transactionRows;
    }
}

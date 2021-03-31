package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionMode;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TransactionTable extends SQLiteOpenHelper {
    public static final String TRANSACTION_TABLE = "TRANSACTION_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_SHARES = "shares";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
    public static final String COLUMN_CREATED_AT = "transaction_date";

    public TransactionTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPortfolioTable =
                "CREATE TABLE "
                        + TRANSACTION_TABLE
                        + " ("
                        + COLUMN_USERNAME
                        + " TEXT, "
                        + COLUMN_SHARES
                        + " INTEGER, "
                        + COLUMN_SYMBOL
                        + " TEXT, "
                        + COLUMN_TRANSACTION_TYPE
                        + " TEXT, "
                        + COLUMN_PRICE
                        + " REAL, "
                        + COLUMN_CREATED_AT
                        + " TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                        + "FOREIGN KEY("
                        + COLUMN_USERNAME
                        + ") REFERENCES "
                        + UserTable.USER_TABLE
                        + "("
                        + UserTable.COLUMN_USERNAME
                        + "))";

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

    public boolean addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, transaction.getUsername());
        cv.put(COLUMN_SHARES, transaction.getShares());
        cv.put(COLUMN_SYMBOL, transaction.getSymbol());
        cv.put(COLUMN_PRICE, transaction.getPrice());
        cv.put(COLUMN_TRANSACTION_TYPE, transaction.getTransactionTypeString());

        long insert = db.insert(TRANSACTION_TABLE, null, cv);
        return insert >= 0;
    }

    public ArrayList<Transaction> getTransactions(String username) {
        String query =
                String.format("SELECT * FROM %s WHERE %s = ?", TRANSACTION_TABLE, COLUMN_USERNAME);

        return queryTransactions(query, new String[] {username});
    }

    public ArrayList<Transaction> getTransactionsBySymbol(String username, String symbol) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %s = ?",
                        TRANSACTION_TABLE, COLUMN_USERNAME, COLUMN_SYMBOL);
        return queryTransactions(query, new String[] {username, symbol});
    }

    private ArrayList<Transaction> queryTransactions(String query, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Transaction> transactions = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
                int shares = cursor.getInt(cursor.getColumnIndex(COLUMN_SHARES));
                float price = cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE));
                TransactionMode transactionType =
                        TransactionMode.fromString(
                                cursor.getString(cursor.getColumnIndex(COLUMN_TRANSACTION_TYPE)));
                LocalDateTime transactionDate =
                        Transaction.getCreatedAtFromString(
                                cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_AT)));
                transactions.add(
                        new Transaction(
                                username, symbol, shares, price, transactionType, transactionDate));

            } while (cursor.moveToNext());
        }
        return transactions;
    }
}

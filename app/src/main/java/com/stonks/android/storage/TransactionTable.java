package com.stonks.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.stonks.android.BuildConfig;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionFilters;
import com.stonks.android.model.TransactionMode;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TransactionTable extends SQLiteOpenHelper {
    private static TransactionTable transactionTable;

    public static final String TABLE_NAME = "TRANSACTION_TABLE";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_SHARES = "shares";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
    public static final String COLUMN_CREATED_AT = "transaction_date";
    public static final String COLUMN_COMPUTED_AMOUNT = COLUMN_PRICE + " * " + COLUMN_SHARES;
    public static final String CREATE_STRING =
            "CREATE TABLE "
                    + TABLE_NAME
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
                    + UserTable.TABLE_NAME
                    + "("
                    + UserTable.COLUMN_USERNAME
                    + "))";

    private TransactionTable(@Nullable Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, 7);
    }

    public static TransactionTable getInstance(Context context) {
        if (transactionTable == null) {
            transactionTable = new TransactionTable(context);
        }

        return transactionTable;
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

    public boolean addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, transaction.getUsername());
        cv.put(COLUMN_SHARES, transaction.getShares());
        cv.put(COLUMN_SYMBOL, transaction.getSymbol());
        cv.put(COLUMN_PRICE, transaction.getPrice());
        cv.put(COLUMN_TRANSACTION_TYPE, transaction.getTransactionTypeString());

        long insert = db.insert(TABLE_NAME, null, cv);
        return insert >= 0;
    }

    public ArrayList<Transaction> getTransactions(String username) {
        String query = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COLUMN_USERNAME);

        return queryTransactions(query, new String[] {username});
    }

    public ArrayList<Transaction> getTransactionsBySymbol(String username, String symbol) {
        String query =
                String.format(
                        "SELECT * FROM %s WHERE %s = ? AND %s = ?",
                        TABLE_NAME, COLUMN_USERNAME, COLUMN_SYMBOL);
        return queryTransactions(query, new String[] {username, symbol});
    }

    public ArrayList<Transaction> filterTransactions(TransactionFilters filters) {
        String query = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COLUMN_USERNAME);
        ArrayList<String> selectionArgsList = new ArrayList();
        selectionArgsList.add(filters.getUsername());

        if (filters.isMinAmountFilterApplied()) {
            query =
                    query
                            + String.format(
                                    "  AND %s >= %s",
                                    COLUMN_COMPUTED_AMOUNT,
                                    Integer.toString(filters.getMinAmount()));
        }

        if (filters.isMaxAmountFilterApplied()) {
            query =
                    query
                            + String.format(
                                    " AND %s <= %s",
                                    COLUMN_COMPUTED_AMOUNT,
                                    Integer.toString(filters.getMaxAmount()));
        }

        if (filters.isModeFilterApplied()) {
            query = query + String.format(" AND %s = ?", COLUMN_TRANSACTION_TYPE);
            selectionArgsList.add(filters.getMode().toString());
        }

        if (filters.isSymbolFilterApplied()) {
            query =
                    query
                            + String.format(" AND %s IN ", COLUMN_SYMBOL)
                            + filters.getSymbolsAsQueryString();
        }

        return queryTransactions(
                query, selectionArgsList.toArray(new String[selectionArgsList.size()]));
    }

    public ArrayList<String> getSymbols(String username) {
        String query =
                String.format(
                        "SELECT %s FROM %s WHERE %s = ?",
                        COLUMN_SYMBOL, TABLE_NAME, COLUMN_USERNAME);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] {username});
        ArrayList<String> symbols = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String symbol = cursor.getString(cursor.getColumnIndex(COLUMN_SYMBOL));
                symbols.add(symbol);
            } while (cursor.moveToNext());
        }

        return symbols;
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

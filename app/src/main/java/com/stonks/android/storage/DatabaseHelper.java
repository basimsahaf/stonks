package com.stonks.android.storage;

import android.database.sqlite.SQLiteDatabase;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper {
    public static void removeAllTables(SQLiteDatabase db) {
        final List<String> tables =
                Arrays.asList(
                        UserTable.TABLE_NAME,
                        FavouritesTable.TABLE_NAME,
                        TransactionTable.TABLE_NAME,
                        PortfolioTable.TABLE_NAME,
                        CompanyTable.TABLE_NAME);
        tables.forEach(table -> db.execSQL("DROP TABLE IF EXISTS " + table));
    }

    public static void createAllTables(SQLiteDatabase db) {
        final List<String> createStrings =
                Arrays.asList(
                        UserTable.CREATE_STRING,
                        FavouritesTable.CREATE_STRING,
                        TransactionTable.CREATE_STRING,
                        PortfolioTable.CREATE_STRING,
                        CompanyTable.CREATE_STRING);
        createStrings.forEach(db::execSQL);
    }
}

package com.stonks.android.model;

import java.util.ArrayList;

public class Transactions {
    private final ArrayList<TransactionRow> transactionRows;

    public Transactions(ArrayList<TransactionRow> transactionRows) {
        this.transactionRows = transactionRows;
    }

    public ArrayList<TransactionRow> getTransactionRows() {
        return transactionRows;
    }
}

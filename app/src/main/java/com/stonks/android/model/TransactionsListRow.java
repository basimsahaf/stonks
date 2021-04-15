package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TransactionsListRow {
    // TransactionListRow is either a date or a transaction
    private final LocalDateTime date;
    private final Transaction transaction;

    public TransactionsListRow(LocalDateTime date) {
        this.date = date;
        this.transaction = null;
    }

    public TransactionsListRow(Transaction transaction) {
        this.date = null;
        this.transaction = transaction;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM yy");
        return date.atZone(ZoneId.systemDefault()).format(format);
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

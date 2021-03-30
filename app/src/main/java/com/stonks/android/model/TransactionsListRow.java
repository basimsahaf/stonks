package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionsListRow {
    private LocalDateTime date;
    private Transaction transaction;

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
        return date.format(format);
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

package com.stonks.android.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Formatters {
    public static String formatPrice(Float price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPrice(Double price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPricePerShare(int numShares, Float pricePerShare) {
        return String.format(Locale.CANADA, "%d shares @ $%.2f", numShares, pricePerShare);
    }

    public static String formatPricePerShare(int numShares, Double pricePerShare) {
        return String.format(Locale.CANADA, "%d shares @ $%.2f", numShares, pricePerShare);
    }

    public static String formatPriceChange(Float priceChange, Float changePercent) {
        if (changePercent < 0.01) {
            return String.format(Locale.CANADA, "$%.2f (0.01%%)", Math.abs(priceChange));
        }
        return String.format(Locale.CANADA, "$%.2f (%.2f%%)", Math.abs(priceChange), Math.abs(changePercent));
    }

    public static String formatPriceChange(Double priceChange, Double changePercent) {
        return String.format(Locale.CANADA, "$%.2f (%.2f%%)", priceChange, changePercent);
    }

    public static String formatStockQuantity(Float price, int multiplier) {
        return String.format(Locale.CANADA, "$%.2f (x%d)", price, multiplier);
    }

    public static String formatStockQuantity(Double price, int multiplier) {
        return String.format(Locale.CANADA, "$%.2f (x%d)", price, multiplier);
    }

    public static String formatDateISO8601(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.CANADA);
        return sdf.format(date);
    }

    public static String formatTotalReturn(Float amount, LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM YYYY", Locale.CANADA);
        String dateString = formatter.format(date);

        if (amount < 0) {
            amount *= -1.0f;
            return String.format(Locale.CANADA, "$%.2f loss made since %s", amount, dateString);
        }

        return String.format(Locale.CANADA, "$%.2f profit made since %s", amount, dateString);
    }
}

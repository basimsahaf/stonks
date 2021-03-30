package com.stonks.android.utility;

import java.util.Locale;

public class Formatters {
    public static String formatPrice(Float price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPrice(Double price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPriceChange(Float priceChange, Float changePercent) {
        return String.format(Locale.CANADA, "$%.2f (%.2f%%)", priceChange, changePercent);
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
}

package com.stonks.android.utility;

import java.util.Locale;

public class Formatters {
    public static String formatPrice(Float price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPrice(Double price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }
}

package com.stonks.android.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatters {
    public static String formatPrice(Float price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatPrice(Double price) {
        return String.format(Locale.CANADA, "$%.2f", price);
    }

    public static String formatDateISO8601(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.CANADA);
        return sdf.format(new Date());
    }
}

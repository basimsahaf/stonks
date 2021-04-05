package com.stonks.android.utility;

import com.stonks.android.model.BarData;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class ChartHelpers {
    public static int getDataPointLimit(DateRange range) {
        switch (range) {
            case MONTH:
                return 217;
            case YEAR:
                return 365;
            case THREE_YEARS:
                return 1000;
            case DAY:
            case WEEK:
            default:
                return 390;
        }
    }

    public static AlpacaTimeframe getDataPointTimeframe(DateRange range) {
        switch (range) {
            case WEEK:
            case MONTH:
                return AlpacaTimeframe.MINUTES_15;
            case YEAR:
            case THREE_YEARS:
                return AlpacaTimeframe.DAY;
            case DAY:
            default:
                return AlpacaTimeframe.MINUTE;
        }
    }

    public static List<BarData> mergeBars(List<BarData> bars, int windowSize) {
        List<BarData> clubbedBars = new ArrayList<>();

        for (int barIndex = 0; barIndex < bars.size(); barIndex += windowSize) {
            int endIndex = Math.min(barIndex + windowSize, bars.size() - 1);
            if (barIndex == endIndex) break;

            BarData newBar = mergeBars(bars.subList(barIndex, endIndex));
            clubbedBars.add(newBar);
        }

        return clubbedBars;
    }

    private static BarData mergeBars(List<BarData> bars) {
        BarData barData = new BarData(-1, -1, -1, -1, -1);

        barData.setHigh(
                bars.stream().map(BarData::getHigh).max(Comparator.naturalOrder()).orElse(0f));
        barData.setLow(
                bars.stream().map(BarData::getLow).min(Comparator.naturalOrder()).orElse(0f));
        barData.setOpen(bars.get(0).getOpen());
        barData.setTimestamp(bars.get(0).getTimestamp());
        barData.setClose(bars.get(bars.size() - 1).getClose());
        barData.setEndTimestamp(bars.get(bars.size() - 1).getTimestamp());

        return barData;
    }

    public static List<BarData> getSameDayBars(List<BarData> bars) {
        BarData lastBar = bars.get(bars.size() - 1);
        LocalDateTime day =
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(lastBar.getTimestamp()),
                        TimeZone.getDefault().toZoneId());

        LocalDateTime firstEntry = day.withHour(9).withMinute(30).withSecond(0);
        long firstTimeStamp = firstEntry.atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

        List<BarData> sameDayBars = new ArrayList<>();

        for (BarData bar : bars) {
            if (bar.getTimestamp() < firstTimeStamp) continue;

            sameDayBars.add(bar);
        }

        return sameDayBars;
    }


    public static DateTimeFormatter getMarkerDateFormatter(DateRange range) {
        switch (range) {
            case DAY:
            default:
                return DateTimeFormatter.ofPattern("HH:mm");
            case WEEK:
            case MONTH:
                return DateTimeFormatter.ofPattern("HH:mm MMM dd");
            case YEAR:
            case THREE_YEARS:
                return DateTimeFormatter.ofPattern("MMM dd, yyyy");
        }
    }


    public static String getIsoStartDate(DateRange range) {
        if (range == DateRange.DAY) {
            // for a single day chart, we fetch 390 data points then filter
            // this is to account for holidays since there is
            // no stock data available when markets are closed
            return "";
        }

        LocalDateTime now = LocalDateTime.now();

        switch (range) {
            case WEEK:
                now = now.minusDays(7);
                break;
            case MONTH:
                now = now.minusMonths(1);
                break;
            case YEAR:
                now = now.minusYears(1);
                break;
            case THREE_YEARS:
                now = now.minusYears(3);
                break;
        }

        ZonedDateTime dateTime =
                ZonedDateTime.ofInstant(
                        now.atZone(TimeZone.getDefault().toZoneId()).toInstant(),
                        ZoneId.systemDefault());
        DateTimeFormatter df = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        return df.format(dateTime);
    }
}

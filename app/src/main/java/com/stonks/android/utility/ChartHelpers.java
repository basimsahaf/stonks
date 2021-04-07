package com.stonks.android.utility;

import android.util.Log;
import com.stonks.android.model.BarData;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class ChartHelpers {
    public static LocalTime startOfDayTime = LocalTime.of(9, 30, 0, 0);
    public static LocalTime endOfDayTime = LocalTime.of(16, 0, 0, 0);

    public static int getDataPointLimit(DateRange range) {
        switch (range) {
            case MONTH:
                return 1000;
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
        if (windowSize == 1) {
            return bars;
        }

        List<BarData> clubbedBars = new ArrayList<>();

        for (int barIndex = 0; barIndex < bars.size(); barIndex += windowSize) {
            int endIndex = Math.min(barIndex + windowSize, bars.size() - 1);
            if (barIndex == endIndex) break;

            Log.d(
                    "Merging",
                    "from "
                            + convertEpochToDateTime(bars.get(barIndex).getTimestamp()).toString()
                            + " to "
                            + convertEpochToDateTime(bars.get(endIndex).getTimestamp()).toString());
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
        barData.setEndTimestamp(bars.get(bars.size() - 1).getEndTimestamp());

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

    public static LocalDateTime convertEpochToDateTime(long time) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(time), TimeZone.getDefault().toZoneId());
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
                now = now.minusDays(7).withHour(9).withMinute(30);
                break;
            case MONTH:
                now = now.minusMonths(1).withHour(9).withMinute(30);
                break;
            case YEAR:
                now = now.minusYears(1).withHour(9).withMinute(30);
                break;
            case THREE_YEARS:
                now = now.minusYears(3).withHour(9).withMinute(30);
                break;
        }

        ZonedDateTime dateTime =
                ZonedDateTime.ofInstant(
                        now.atZone(TimeZone.getDefault().toZoneId()).toInstant(),
                        ZoneId.systemDefault());
        DateTimeFormatter df = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        return df.format(dateTime);
    }

    private static boolean isEndOfDayTime(LocalTime time) {
        if (time.getHour() == endOfDayTime.getHour()
                && time.getMinute() == endOfDayTime.getMinute()) {
            return true;
        }

        return (endOfDayTime.getHour() - time.getHour() == 1)
                && (endOfDayTime.getMinute() - time.getMinute() <= 30);
    }

    public static List<BarData> cleanData(List<BarData> data, AlpacaTimeframe timeframe) {
        long second = 60;
        int multiplier = 1;

        switch (timeframe) {
            case MINUTE:
                multiplier = 1;
                break;
            case MINUTES_5:
                multiplier = 5;
                break;
            case MINUTES_15:
                multiplier = 15;
                break;
            case DAY:
                multiplier = 24 * 60;
                break;
        }

        long difference = second * multiplier;

        Log.d("Cleaning Data", "expected difference: " + difference);

        List<BarData> cleanData = new ArrayList<>();
        BarData bar = data.get(0);
        cleanData.add(bar);
        long lastTimeStamp = data.get(0).getTimestamp();
        int i = 1;

        while (i < data.size()) {
            BarData currentBar = data.get(i);

            Log.d(
                    "Cleaning data",
                    "i="
                            + i
                            + ", lastTimeStamp="
                            + lastTimeStamp
                            + ", current="
                            + currentBar.getTimestamp());
            if (currentBar.getTimestamp() - lastTimeStamp > difference) {
                LocalDateTime dateTime = convertEpochToDateTime(lastTimeStamp);

                if (timeframe == AlpacaTimeframe.MINUTES_15) {
                    // check if lastTimeStamp was around 4:00PM
                    LocalTime time = dateTime.toLocalTime();
                    Log.d(
                            "Cleaning data",
                            "i="
                                    + i
                                    + ", checking if "
                                    + lastTimeStamp
                                    + " ("
                                    + time.toString()
                                    + ") is EOD");
                    if (isEndOfDayTime(time)) {
                        Log.d("Cleaning data", "i=" + i + " was new day");
                        lastTimeStamp = currentBar.getTimestamp();
                        cleanData.add(currentBar);
                        i++;
                        continue;
                    }
                } else if (timeframe == AlpacaTimeframe.DAY) {
                    // Check if lastTimeStamp was Friday
                    if (dateTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        lastTimeStamp = currentBar.getTimestamp();
                        cleanData.add(currentBar);
                        i++;
                        continue;
                    }
                }

                lastTimeStamp = lastTimeStamp + difference;

                BarData newBar =
                        new BarData(
                                (int) (lastTimeStamp),
                                currentBar.getHigh(),
                                currentBar.getLow(),
                                currentBar.getOpen(),
                                currentBar.getClose());
                Log.d("Cleaning data", "i=" + i + ", adding entry with timestamp=" + lastTimeStamp);
                cleanData.add(newBar);
            } else {
                if (currentBar.getTimestamp() - lastTimeStamp != difference) {
                    Log.d("Cleaning data", "entry timestamp less than expected difference");
                    currentBar.setTimestamp((int) (lastTimeStamp + difference));
                }

                lastTimeStamp = currentBar.getTimestamp();
                cleanData.add(currentBar);
                i++;
            }
        }

        final List<BarData> correctedData = new ArrayList<>();
        cleanData.get(0).setClose(bar.getOpen());

        for (i = 0; i < cleanData.size(); ++i) {
            bar = cleanData.get(i);
            bar.setEndTimestamp((int) (bar.getTimestamp() + difference));
            Log.d(
                    "Correct data",
                    convertEpochToDateTime(bar.getTimestamp()).toString()
                            + " - "
                            + convertEpochToDateTime(bar.getEndTimestamp()).toString());
            correctedData.add(bar);
        }

        Log.d(
                "Cleaning data",
                "timeframe: " + timeframe.toString() + ", data points: " + correctedData.size());

        return correctedData;
    }
}

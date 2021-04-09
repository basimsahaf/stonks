package com.stonks.android.utility;

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

    public static long getEpochTimestamp(DateRange range, long timestamp) {
        LocalDateTime mostRecentDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId());

        LocalDateTime firstDateTime = LocalDateTime.now();

        switch (range) {
            case DAY:
                firstDateTime = mostRecentDateTime.withHour(9).withMinute(30);
                break;
            case WEEK:
                firstDateTime = mostRecentDateTime.minusDays(7).withHour(9).withMinute(30);
                break;
            case MONTH:
                firstDateTime = mostRecentDateTime.minusMonths(1).withHour(9).withMinute(30);
                break;
            case YEAR:
                firstDateTime = mostRecentDateTime.minusYears(1).withHour(9).withMinute(30);
                break;
            case THREE_YEARS:
                firstDateTime = mostRecentDateTime.minusYears(3).withHour(9).withMinute(30);
                break;
        }

        return firstDateTime.atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();
    }

    private static boolean isEndOfDayTime(LocalTime time, AlpacaTimeframe timeframe) {
        if (time.getHour() == endOfDayTime.getHour()
                && time.getMinute() == endOfDayTime.getMinute()) {
            return true;
        }

        return endOfDayTime.minusMinutes((2L * timeframe.toInt()) + 1).isBefore(time);
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

        List<BarData> cleanData = new ArrayList<>();
        BarData bar = data.get(0);
        BarData lastBar = data.get(data.size() - 1);

        // insert 9:30am bar if it doesn't exist
        if (timeframe == AlpacaTimeframe.MINUTE) {
            LocalDateTime dateTime = convertEpochToDateTime(lastBar.getTimestamp());
            LocalDateTime expectedStart = dateTime.withHour(9).withMinute(30);

            long expectedTimestamp =
                    expectedStart.atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

            int i = data.size() - 1;
            while (i >= 0 && data.get(i).getTimestamp() > expectedTimestamp) {
                i--;
            }

            if (data.get(i).getTimestamp() < expectedTimestamp) {
                BarData newBar = data.get(i + 1);

                data.add(
                        i + 1,
                        new BarData(
                                (int) expectedTimestamp,
                                newBar.getHigh(),
                                newBar.getLow(),
                                newBar.getOpen(),
                                newBar.getClose()));
            }
        }

        cleanData.add(bar);
        long lastTimeStamp = data.get(0).getTimestamp();
        int i = 1;

        while (i < data.size()) {
            BarData currentBar = data.get(i);

            if (currentBar.getTimestamp() - lastTimeStamp > difference) {
                LocalDateTime dateTime = convertEpochToDateTime(lastTimeStamp);

                if (timeframe != AlpacaTimeframe.DAY) {
                    // check if lastTimeStamp was around 4:00PM
                    LocalTime time = dateTime.toLocalTime();

                    if (isEndOfDayTime(time, timeframe)) {
                        lastTimeStamp = currentBar.getTimestamp();
                        cleanData.add(currentBar);
                        i++;
                        continue;
                    }
                } else {
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
                cleanData.add(newBar);
            } else {
                if (currentBar.getTimestamp() - lastTimeStamp != difference) {
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
            correctedData.add(bar);
        }

        return correctedData;
    }
}

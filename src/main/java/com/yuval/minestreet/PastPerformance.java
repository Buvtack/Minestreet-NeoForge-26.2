package com.yuval.minestreet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PastPerformance {

    private Double day;
    private Double week;
    private Double month;
    private Double half;
    private Double ytd;
    private Double year;
    private Double fiveYears;
    private Double tenYears;
    private Double allTime;

    private PastPerformance() {}

    public static PastPerformance fromStock(JsonObject stock) {
        PastPerformance performance = new PastPerformance();
        performance.day = day(stock);
        performance.week = week(stock);
        performance.month = month(stock);
        performance.half = half(stock);
        performance.ytd = ytd(stock);
        performance.year = year(stock);
        performance.fiveYears = fiveYears(stock);
        performance.tenYears = tenYears(stock);
        performance.allTime = allTime(stock);
        return performance;
    }

    public String str(Double d) {
        return Double.toString(d);
    }

    public Double day() {
        return day;
    }

    private static Double day(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.DAY_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double week() {
        return week;
    }

    private static Double week(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.WEEK_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double month() {
        return month;
    }

    private static Double month(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.MONTH_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double half() {
        return half;
    }

    private static Double half(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.HALF_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double ytd() {
        return ytd;
    }

    private static Double ytd(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.YTD_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double year() {
        return year;
    }

    private static Double year(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.YEAR_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double fiveYears() {
        return fiveYears;
    }

    private static Double fiveYears(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.FIVE_YEAR_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double tenYears() {
        return tenYears;
    }

    private static Double tenYears(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.TEN_YEAR_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public Double allTime() {
        return allTime;
    }

    private static Double allTime(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.ALL_TIME_RETURN).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonObject fetch(String ticker, String range, String interval) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker
                + "?range=" + range + "&interval=" + interval;

        HttpResponse<String> response = StockMarket.getHttp(url);
        if (response.statusCode() == 200)
            return JsonParser.parseString(response.body()).getAsJsonObject();

        WolfOfMinestreet.LOGGER.error("Error fetching past performance (range & interval):\n" + response.body());
        return null;
    }

    public static JsonObject fetch(String ticker, Dateable dateable) {
        long p1 = 0;
        long p2 = 0;
        if (dateable != null) {
            ZonedDateTime date = dateable.date();
            p1 = date.minusDays(4).toEpochSecond();
            p2 = date.toEpochSecond();
        }

        String url = dateable != null ? "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker
                + "?period1=" + p1 + "&period2=" + p2 + "&interval=1d" :
                "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker
                        + "?range=max&interval=1mo";

        HttpResponse<String> response = StockMarket.getHttp(url);

        if (response.statusCode() == 200)
            return JsonParser.parseString(response.body()).getAsJsonObject();

        WolfOfMinestreet.LOGGER.error("Error fetching past performance: \n" + response.body());
        return null;
    }

    public static Double getPastReturn(JsonObject chart, Dateable dateable) {
        if (chart == null)
            return null;

        long date = dateable.date().toEpochSecond();
        JsonObject result = chart.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject();
        JsonArray timestamp = result.getAsJsonArray("timestamp");
        long proximity = Long.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < timestamp.size(); i++) {
            long current = timestamp.get(i).getAsLong();
            if (Math.abs(date - current) < proximity) {
                proximity = Math.abs(date - current);
                index = i;
            }
        }

        JsonArray adjOpenings = getAdjOpenings(chart);
        double open = adjOpenings.get(index).getAsDouble();
        double price = getMeta(chart).get(StockMarketKeys.PRICE).getAsDouble();
        return price / open * 100 - 100;
    }

    public static Double getAllTimeReturn(JsonObject chart) {
        if (chart == null)
            return null;

        JsonArray adjOpenings = getAdjOpenings(chart);

        for (int i = 0; i < adjOpenings.size(); i++) {
            if (!adjOpenings.get(i).equals("null")) {
                double adjOpening = adjOpenings.get(i).getAsDouble();
                double price = getMeta(chart).get(StockMarketKeys.PRICE).getAsDouble();
                return price / adjOpening * 100 - 100;
            }
        }

        return null;
    }

    private static JsonObject getMeta(JsonObject chart) {
        return chart.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("meta");
    }

    private static JsonArray getAdjOpenings(JsonObject chart) {
        JsonObject result = chart.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject();
        JsonObject indicators = result.getAsJsonObject("indicators");
        JsonObject quote = indicators.getAsJsonArray("quote").get(0).getAsJsonObject();
        JsonArray openings = quote.getAsJsonArray("open");
        JsonArray closings = quote.getAsJsonArray("close");
        JsonArray adjClosings = indicators.getAsJsonArray("adjclose").get(0).getAsJsonObject().getAsJsonArray("adjclose");
        JsonArray adjOpenings = new JsonArray();
        for (int i = 0; i < adjClosings.size(); i++) {
            JsonElement openingElement = openings.get(i);
            JsonElement closingElement = closings.get(i);
            JsonElement adjClosingElement = adjClosings.get(i);
            if (!openingElement.isJsonNull() && !closingElement.isJsonNull() && !adjClosingElement.isJsonNull()) {
                double adjOpening = openingElement.getAsDouble() * adjClosingElement.getAsDouble() / closingElement.getAsDouble();
                adjOpenings.add(Double.toString(adjOpening));
            } else
                adjOpenings.add("null");
        }

        return adjOpenings;
    }
}

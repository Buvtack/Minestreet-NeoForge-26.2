package com.yuval.minestreet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.network.packets.InitStockPacket;
import com.yuval.minestreet.network.packets.SyncStockPacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StockMarket {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static Map<String, JsonObject> storedStocks = new ConcurrentHashMap<>();
    public static final String[] INITIAL_TICKERS = { "VOO", "QQQ", "NVDA", "GOOGL", "AAPL", "TSLA", "MSFT", "META", "AMZN", "SMH", "VTI" };

//    private static final String VOO = "{\"symbol\":\"VOO\",\"currency\":\"USD\",\"regularMarketPrice\":703.078,\"chartPreviousClose\":696.71}";
//    private static final String QQQ = "{\"symbol\":\"QQQ\",\"currency\":\"USD\",\"regularMarketPrice\":7014.47,\"chartPreviousClose\":708.69}";
//    private static final String NVDA = "{\"symbol\":\"NVDA\",\"currency\":\"USD\",\"regularMarketPrice\":219.62,\"chartPreviousClose\":218.36}";
//    private static final String GOOGL = "{\"symbol\":\"GOOGL\",\"currency\":\"USD\",\"regularMarketPrice\":338.0,\"chartPreviousClose\":332.6}";
//    private static final String AAPL = "{\"symbol\":\"AAPL\",\"currency\":\"USD\",\"regularMarketPrice\":329.28,\"chartPreviousClose\":326.57}";
//    private static final String TSLA = "{\"symbol\":\"TSLA\",\"currency\":\"USD\",\"regularMarketPrice\":364.258,\"chartPreviousClose\":363.56}";
//    private static final String MSFT = "{\"symbol\":\"MSFT\",\"currency\":\"USD\",\"regularMarketPrice\":489.13,\"chartPreviousClose\":492.44}";
//    private static final String META = "{\"symbol\":\"META\",\"currency\":\"USD\",\"regularMarketPrice\":654.256,\"chartPreviousClose\":644.38}";
//    private static final String AMZN = "{\"symbol\":\"AMZN\",\"currency\":\"USD\",\"regularMarketPrice\":255.294,\"chartPreviousClose\":251.89}";
//    private static final String SMH = "{\"symbol\":\"SMH\",\"currency\":\"USD\",\"regularMarketPrice\":555.56,\"chartPreviousClose\":560.28}";
//    private static final String VTI = "{\"symbol\":\"VTI\",\"currency\":\"USD\",\"regularMarketPrice\":371.67,\"chartPreviousClose\":373.22}";

    //private static final String[] INITIAL_STOCKS = { VOO, QQQ, NVDA, GOOGL, AAPL, TSLA, MSFT, META, AMZN, SMH, VTI };

    public static void initialize() {
        CompletableFuture.runAsync(() -> {
            for (String ticker : INITIAL_TICKERS)
                fetchAndStore(ticker);
        });
//        for (int i = 0; i < INITIAL_TICKERS.length; i++) {
//            JsonObject stock = JsonParser.parseString(INITIAL_STOCKS[i]).getAsJsonObject();
//            storedStocks.put(INITIAL_TICKERS[i], stock);
//        }
    }

    public static void fetchAndStore(String ticker) {
        ticker = URLEncoder.encode(ticker, StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://query1.finance.yahoo.com/v8/finance/chart/" + ticker))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            WolfOfMinestreet.LOGGER.info(response.body());

            JsonObject responseInJson = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject dataToStore = extractData(responseInJson);

            WolfOfMinestreet.LOGGER.info(dataToStore.toString());

            storedStocks.put(ticker, dataToStore);
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
            WolfOfMinestreet.LOGGER.error("Could not fetch the initial stocks from the API");
        }
    }

    private static JsonObject extractData(JsonObject raw) {
        JsonObject dataToStore = new JsonObject();
        JsonObject nestedData = raw.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("meta");
        dataToStore.add(StockMarketKeys.TICKER, nestedData.get(StockMarketKeys.TICKER));
        dataToStore.add(StockMarketKeys.CURRENCY, nestedData.get(StockMarketKeys.CURRENCY));
        dataToStore.add(StockMarketKeys.PRICE, nestedData.get(StockMarketKeys.PRICE));
        dataToStore.add(StockMarketKeys.CHART_PREVIOUS_CLOSE, nestedData.get(StockMarketKeys.CHART_PREVIOUS_CLOSE));
        dataToStore.add(StockMarketKeys.NAME, nestedData.get(StockMarketKeys.NAME));
        dataToStore.add(StockMarketKeys.FETCHED_TIME, JsonParser.parseString(Long.toString(System.nanoTime())));
        return dataToStore;
    }

    public static void searchAndStore(String searched) {
        if (searched.isBlank())
            return;

        //searched = searched.toUpperCase().trim();
        searched = URLEncoder.encode(searched, StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://query1.finance.yahoo.com/v1/finance/search?q=" + searched.toUpperCase() + "&quotesCount=5&newsCount=0"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject responseInJson = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray array = responseInJson.getAsJsonArray("quotes");
            array.forEach(element -> {
                String ticker = element.getAsJsonObject().get(StockMarketKeys.TICKER).getAsString();
                String type = element.getAsJsonObject().get(StockMarketKeys.QUOTE_TYPE).getAsString().toLowerCase();
                if (type.equals("option") || type.equals("future") || type.equals("index") || type.equals("currency"))
                    return;

                get(ticker);
            });
            WolfOfMinestreet.LOGGER.info(response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            WolfOfMinestreet.LOGGER.error("Could not search the ticker");
        }
    }

    public static List<JsonObject> search(String ticker) {
        ticker = ticker.toUpperCase().trim();
        Set<String> tickers = storedStocks.keySet();
        List<JsonObject> result = new LinkedList<>();
        for (String storedTicker : tickers) {
            if (storedTicker.contains(ticker)) {
                result.add(storedStocks.get(storedTicker));
            }
        }

        return null;
    }

    public static JsonObject get(String ticker) {
        ticker = ticker.toUpperCase().trim();
        if (!storedStocks.containsKey(ticker))
            fetchAndStore(ticker);

        return storedStocks.get(ticker);
    }

    public static String getPrice(String ticker) {
        JsonObject data = get(ticker);
        if (data == null)
            return "0";

        String priceStr = data.get(StockMarketKeys.PRICE).getAsString();
        double price = Double.parseDouble(priceStr);
        BigDecimal result = new BigDecimal(price);
        return result.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static String getPreviousClosePrice(String ticker) {
        JsonObject data = get(ticker);
        if (data == null)
            return "0";

        return data.get(StockMarketKeys.CHART_PREVIOUS_CLOSE).getAsString();
    }

    public static String getChange(String ticker) {
        String price = getPrice(ticker);
        String previousClose = getPreviousClosePrice(ticker);

        double priceInt = Double.parseDouble(price);
        double previousCloseInt = Double.parseDouble(previousClose);

        BigDecimal result = new BigDecimal(priceInt - previousCloseInt);
        return result.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static String getChangePercentage(String ticker) {
        String previousClose = getPreviousClosePrice(ticker);
        String change = getChange(ticker);

        double previousCloseInt = Double.parseDouble(previousClose);
        if (previousCloseInt == 0)
            return "Inf";

        double changeInt = Double.parseDouble(change);
        if (changeInt == 0)
            return "0";

        double percentage = changeInt / previousCloseInt * 100;
        BigDecimal result = new BigDecimal(percentage);
        return result.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static void clean() {
        storedStocks.entrySet().removeIf(entry -> {
            JsonObject data = entry.getValue();
            long fetchedTime = data.get(StockMarketKeys.FETCHED_TIME).getAsLong();
            return System.nanoTime() - fetchedTime >= 300_000_000_000L;
        });
    }
}

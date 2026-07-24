package com.yuval.minestreet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StockMarket {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getPrice(String ticker) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://query1.finance.yahoo.com/v8/finance/chart/" + ticker))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return extract(response.body(), StockMarketKeys.PRICE);

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private static String extract(String body, String key) {
        int beginIndex = body.indexOf(key);
        int i = beginIndex;
        String result = "";
        while (body.charAt(i) != ':') {
            i++;
        }

        i++;

        while (body.charAt(i) != ',')
            result += body.charAt(i++);

        return result;
    }
}

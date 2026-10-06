package com.yuval.minestreet;

import com.google.gson.*;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.common.Order;
import com.yuval.minestreet.common.Position;
import com.yuval.minestreet.network.packets.OrderResponsePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StockMarket {

    private static final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .cookieHandler(cookies)
            .build();

    private static volatile String crumb;

    public static Map<String, JsonObject> storedStocks = new ConcurrentHashMap<>();
    public static final String[] INITIAL_TICKERS = { "VOO", "QQQ", "NVDA", "GOOGL", "AAPL", "TSLA", "MSFT", "META", "AMZN", "SMH", "VTI" };
    public static final long CLEAN_INTERVAL = 900_000_000_000L; // 15 minutes

    public static void initialize() {
        heal();
        CompletableFuture.runAsync(() -> {
            for (String ticker : INITIAL_TICKERS)
                fetchAndStore(ticker);
        });
    }

    private static void heal() {
        Path positionsPath = getOrInitPositionPath();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(positionsPath, "*.json")) {
            for (Path positionFile : stream) {
                boolean shouldBeSaved = false;
                JsonObject root = Position.getFileAsJsonObject(positionFile);
                if (!root.has(StockMarketKeys.DIVIDENDS)) {
                    root.add(StockMarketKeys.DIVIDENDS, new JsonObject());
                    shouldBeSaved = true;
                }

                if (!root.has(StockMarketKeys.POSITIONS)) {
                    root.add(StockMarketKeys.POSITIONS, new JsonArray());
                    shouldBeSaved = true;
                }

                JsonArray positions = root.getAsJsonArray(StockMarketKeys.POSITIONS);
                List<JsonObject> positionsToDelete = new ArrayList<>();
                for (JsonElement element : positions) {
                    JsonObject position = element.getAsJsonObject();
                    if (!position.has(StockMarketKeys.DATE)) {
                        position.addProperty(StockMarketKeys.DATE, Instant.now().getEpochSecond());
                        shouldBeSaved = true;
                    }

                    String[] requiredFields = {StockMarketKeys.TICKER, StockMarketKeys.ITEM, StockMarketKeys.AMOUNT, StockMarketKeys.POSITION_PRICE};
                    if (!includesAll(position, requiredFields)) {
                        positionsToDelete.add(position);
                        shouldBeSaved = true;
                    }

                    if (!position.has(StockMarketKeys.OWNER)) {
                        position.addProperty(StockMarketKeys.OWNER, filename(positionFile));
                        shouldBeSaved = true;
                    }
                }
                for (JsonObject toDelete : positionsToDelete)
                    positions.remove(toDelete);

                if (shouldBeSaved)
                    savePositionFile(positionFile, root);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static boolean includesAll(JsonObject object, String[] keys) {
        for (String key : keys) {
            if (!object.has(key))
                return false;
        }

        return true;
    }

    private static String filename(Path file) {
        String filename = file.getFileName().toString();
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    public static void fetchAndStore(String ticker) {
        //fetchAndStoreSummary(ticker);
        ticker = URLEncoder.encode(ticker, StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://query1.finance.yahoo.com/v8/finance/chart/" + ticker + "?range=1y&interval=1d&events=div,splits"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            WolfOfMinestreet.LOGGER.info(response.body());

            JsonObject responseInJson = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray fundamentals = getFundamentalData(ticker);

            JsonObject dataToStore = extractData(responseInJson, fundamentals);

            WolfOfMinestreet.LOGGER.info(dataToStore.toString());

            storedStocks.put(ticker, dataToStore);
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
            WolfOfMinestreet.LOGGER.error("Could not fetch the initial stocks from the API");
        }
    }

    private static JsonArray getFundamentalData(String ticker) {
        if (crumb == null)
            getCrumb();

        //ticker = URLEncoder.encode(ticker, StandardCharsets.UTF_8);
        for (int attempt = 0; attempt < 2; attempt++) {
            String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols="
                    + ticker
                    + "&crumb=" + URLEncoder.encode(crumb, StandardCharsets.UTF_8);

            HttpResponse<String> response = getHttp(url);

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                getCrumb();
                continue;
            }
            if (response.statusCode() != 200)
                throw new IllegalStateException("Quote failed: HTTP " + response.statusCode());

            return JsonParser.parseString(response.body()).getAsJsonObject()
                    .getAsJsonObject("quoteResponse")
                    .getAsJsonArray("result");
        }

        throw new IllegalStateException("Quote failed after crumb refresh");
    }

    private static HttpResponse<String> getHttp(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36")
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void getCrumb() {
        getHttp("https://fc.yahoo.com");
        HttpResponse<String> r = getHttp("https://query1.finance.yahoo.com/v1/test/getcrumb");
        String body = r.body() == null ? "" : r.body().trim();

        if (r.statusCode() != 200 || body.isEmpty() || body.startsWith("<") || body.contains("Too Many"))
            throw new IllegalStateException("Crumb fetch failed: HTTP " + r.statusCode() + " " + body);
        crumb = body;
    }

    private static JsonObject extractData(JsonObject raw, JsonArray fundamentals) {
        JsonObject dataToStore = new JsonObject();
        extractChartData(raw, dataToStore);
        extractFundamentals(fundamentals, dataToStore);
        return dataToStore;
    }

    private static void extractFundamentals(JsonArray fundamentals, JsonObject dataToStore) {
        JsonObject nestedData = !fundamentals.isEmpty() ? fundamentals.get(0).getAsJsonObject() : null;
        JsonObject fundamentalsJson = new JsonObject();
        if (nestedData == null) {
            return;
        }

        if (nestedData.has(StockMarketKeys.AUM))
            dataToStore.add(StockMarketKeys.AUM, nestedData.get(StockMarketKeys.AUM));
        if (nestedData.has(StockMarketKeys.MARKET_CAP))
            dataToStore.add(StockMarketKeys.MARKET_CAP, nestedData.get(StockMarketKeys.MARKET_CAP));

        dataToStore.add(StockMarketKeys.ASSET_TYPE, nestedData.get(StockMarketKeys.ASSET_TYPE));
        dataToStore.add(StockMarketKeys.DIVIDEND_YIELD, nestedData.get(StockMarketKeys.DIVIDEND_YIELD));

        if (nestedData.has(StockMarketKeys.PE_RATIO))
            dataToStore.add(StockMarketKeys.PE_RATIO, nestedData.get(StockMarketKeys.PE_RATIO));

        dataToStore.add(StockMarketKeys.FIFTY_TWO_WEEK_HIGH, nestedData.get(StockMarketKeys.FIFTY_TWO_WEEK_HIGH));
        dataToStore.add(StockMarketKeys.FIFTY_TWO_WEEK_LOW, nestedData.get(StockMarketKeys.FIFTY_TWO_WEEK_LOW));
    }

    private static void extractChartData(JsonObject raw, JsonObject dataToStore) {
        JsonObject nestedData = raw.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("meta");
        JsonObject indicators = raw.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("indicators");
        JsonObject events = raw.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("events");
        dataToStore.add(StockMarketKeys.TICKER, nestedData.get(StockMarketKeys.TICKER));
        dataToStore.add(StockMarketKeys.CURRENCY, nestedData.get(StockMarketKeys.CURRENCY));
        dataToStore.add(StockMarketKeys.PRICE, nestedData.get(StockMarketKeys.PRICE));
        dataToStore.add(StockMarketKeys.REGULAR_MARKET_CHANGE_PERCENT, nestedData.get(StockMarketKeys.REGULAR_MARKET_CHANGE_PERCENT));
        dataToStore.add(StockMarketKeys.CHANGE, nestedData.get(StockMarketKeys.CHANGE));
        dataToStore.add(StockMarketKeys.NAME, nestedData.get(StockMarketKeys.NAME));
        dataToStore.add(StockMarketKeys.FETCHED_TIME, JsonParser.parseString(Long.toString(System.nanoTime())));
        JsonArray volume = indicators.getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray(StockMarketKeys.VOLUME);
        dataToStore.addProperty(StockMarketKeys.VOLUME, getVolume(volume));
        //dataToStore.addProperty(StockMarketKeys.DIVIDEND_YIELD, getDivYield(events, nestedData));

        if (getOrInitPositionPath() != null) {
            adjustPositionsForSplit(events, nestedData);
            collectDividends(events, nestedData, dataToStore);
        }
    }

    private static void collectDividends(JsonObject events, JsonObject nestedData, JsonObject dataToStore) {
        if (events == null || !events.has(StockMarketKeys.DIVIDENDS))
            return;

        JsonObject dividends = events.get(StockMarketKeys.DIVIDENDS).getAsJsonObject();
        JsonObject lastDividend = getLastDividend(dividends);
        if (lastDividend == null)
            return;

        Path positionDir = getOrInitPositionPath();
        long today = Instant.now().getEpochSecond();
        long dividendDate = lastDividend.get(StockMarketKeys.DATE).getAsLong();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(positionDir, "*.json")) {
            for (Path positionFile : stream) {
                boolean shouldBeSaved = false;
                JsonObject root = Position.getFileAsJsonObject(positionFile);

                JsonArray positions = root.getAsJsonArray(StockMarketKeys.POSITIONS);
                for (JsonElement element : positions) {
                    JsonObject positionJson = element.getAsJsonObject();
                    Position position = Position.fromJsonObject(positionJson);
                    long date = position.date();
                    if (today >= dividendDate && date < dividendDate && position.getTicker().equalsIgnoreCase(nestedData.get(StockMarketKeys.TICKER).getAsString())) {
                        JsonObject dividendsJson = root.get(StockMarketKeys.DIVIDENDS).getAsJsonObject();
                        String itemIdStr = position.getItem().toString();
                        double exisitingDividends = dividendsJson.has(itemIdStr) ? dividendsJson.get(itemIdStr).getAsDouble() : 0;
                        double divYield = dataToStore.get(StockMarketKeys.DIVIDEND_YIELD).getAsDouble();
                        int numOfDividends = dividends.keySet().size();
                        double newDividend = position.worth() * divYield / numOfDividends;
                        dividendsJson.addProperty(itemIdStr, exisitingDividends + newDividend);
                        shouldBeSaved = true;

                        positionJson.addProperty(StockMarketKeys.DATE, today);
                    }
                }
                if (shouldBeSaved)
                    savePositionFile(positionFile, root);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static JsonObject getLastDividend(JsonObject dividends) {
        JsonObject lastDividend = null;
        long lastDate = Long.MIN_VALUE;
        for (var entry : dividends.entrySet()) {
            JsonObject dividend = entry.getValue().getAsJsonObject();
            long date = dividend.get(StockMarketKeys.DATE).getAsLong();
            if (date > lastDate) {
                lastDate = date;
                lastDividend = dividend;
            }
        }
        return lastDividend;
    }

    private static void adjustPositionsForSplit(JsonObject events, JsonObject nestedData) {
        if (events == null)
            return;

        JsonObject splits = events.getAsJsonObject("splits");
        if (splits == null)
            return;

        JsonObject lastSplit = getLastSplit(splits);
        if (lastSplit == null)
            return;

        Path positionDir = getOrInitPositionPath();
        long today = Instant.now().getEpochSecond();
        long splitDate = lastSplit.get(StockMarketKeys.DATE).getAsLong();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(positionDir, "*.json")) {
            for (Path positionsFile : stream) {
                JsonObject root = Position.getFileAsJsonObject(positionsFile);
                JsonArray positions = root.getAsJsonArray(StockMarketKeys.POSITIONS);
                boolean shouldBeSaved = false;

                for (JsonElement element : positions) {
                    JsonObject positionJson = element.getAsJsonObject();

                    Position position = Position.fromJsonObject(positionJson);
                    long lastUpdate = positionJson.has(StockMarketKeys.DATE) ? positionJson.get(StockMarketKeys.DATE).getAsLong() : today;

                    if (today >= splitDate && splitDate > lastUpdate && position.getTicker().equalsIgnoreCase(nestedData.get(StockMarketKeys.TICKER).getAsString())) {
                        double numerator = lastSplit.get(StockMarketKeys.NUMERATOR).getAsDouble();
                        double denominator = lastSplit.get(StockMarketKeys.DENOMINATOR).getAsDouble();
                        double ratio = numerator / denominator;
                        double price = position.getPrice();
                        positionJson.addProperty(StockMarketKeys.POSITION_PRICE, price / ratio);
                        positionJson.addProperty(StockMarketKeys.DATE, today);
                        shouldBeSaved = true;
                    }
                }
                if (shouldBeSaved)
                    savePositionFile(positionsFile, root);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static JsonObject getLastSplit(JsonObject splits) {
        long date = Long.MIN_VALUE;
        JsonObject last = null;
        for (var entry : splits.entrySet()) {
            long currentDate = entry.getValue().getAsJsonObject().get(StockMarketKeys.DATE).getAsLong();
            if (currentDate > date) {
                date = currentDate;
                last = entry.getValue().getAsJsonObject();
            }
        }

        return last;
    }

    private static double getDivYield(JsonObject events, JsonObject nestedData) {
        if (events == null)
            return 0;

        JsonObject dividends = events.getAsJsonObject(StockMarketKeys.DIVIDENDS);
        if (dividends == null || dividends.keySet().isEmpty())
            return 0;

        double totalAmount = 0;

        long lastDate = 0;
        int numOfDividends = 0;
        for (String key : dividends.keySet()) {
            JsonObject current = dividends.getAsJsonObject(key);
            long date = current.get("date").getAsLong();
            if (date > lastDate) {
                lastDate = date;
                totalAmount = current.get(StockMarketKeys.AMOUNT).getAsDouble();
            }
            numOfDividends++;
        }
        totalAmount *= numOfDividends;

        double price = nestedData.get(StockMarketKeys.PRICE).getAsDouble();
        return totalAmount / price * 100;
    }

    private static long getVolume(JsonArray volume) {
        return volume.size() > 0 ? volume.get(0).getAsLong() : 0;
    }

    public static void searchAndStore(String searched) {
        if (searched.isBlank())
            return;

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
        double price = Double.parseDouble(getPrice(ticker));
        double change = Double.parseDouble(getChange(ticker));
        return Double.toString(price - change);
    }

    public static String getChange(String ticker) {
        JsonObject stock = storedStocks.get(ticker);
        if (stock == null)
            return "0";

        double change = stock.get(StockMarketKeys.CHANGE).getAsDouble();
        return Double.toString(change);
    }

    public static String getChangePercentage(String ticker) {
        JsonObject stock = storedStocks.get(ticker);
        if (stock == null)
            return "0";

        double change = stock.get(StockMarketKeys.REGULAR_MARKET_CHANGE_PERCENT).getAsDouble();
        return Double.toString(change);
    }

    public static String name(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.NAME).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String type(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.ASSET_TYPE).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    public static double price(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.PRICE).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double marketCap(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.MARKET_CAP).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double aum(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.AUM).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double change(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.CHANGE).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double changePercentage(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.REGULAR_MARKET_CHANGE_PERCENT).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double dividendYield(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.DIVIDEND_YIELD).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double peRatio(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.PE_RATIO).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double fiftyTwoWeekHigh(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.FIFTY_TWO_WEEK_HIGH).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static double fiftyTwoWeekLow(JsonObject stock) {
        try {
            return stock.get(StockMarketKeys.FIFTY_TWO_WEEK_LOW).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    public static void clean() {
        Set<String> keptTickers = new HashSet<>();

        for (String ticker : INITIAL_TICKERS) {
            fetchAndStore(ticker);
            keptTickers.add(ticker);
        }

        if (ServerLifecycleHooks.getCurrentServer() != null)
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                JsonObject root = Position.getFileAsJsonObject(getOrInitPathFile(player.getUUID()));
                JsonArray array = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();
                for (JsonElement element : array) {
                    String ticker = element.getAsJsonObject().get(StockMarketKeys.TICKER).getAsString();
                    fetchAndStore(ticker);
                    keptTickers.add(ticker);
                }
            }

        storedStocks.keySet().removeIf(ticker -> !keptTickers.contains(ticker));
    }

    public static void execute(Order order) {
        if (order.type == Order.Type.BUY) {
            buy(order);
        } else {
            sell(order);
        }
    }

    private static void buy(Order order) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        ServerPlayer owner = server.getPlayerList().getPlayer(order.ownerUUID);
        if (owner == null)
            return;

        if (!owner.isCreative() && order.amount > CommonModHelper.getItemCount(owner, order.item))
            return;

        Position position = Position.get(order.ownerUUID, order.item, order.ticker);
        Position result;
        if (position != null) {
            Position newPosition = Position.fromOrder(order);
            Position toSave = position.combine(newPosition);
            toSave.save();

            result = toSave;
        } else {
            Position newPosition = Position.fromOrder(order);
            newPosition.save();
            result = newPosition;
        }

        String positionStr = result.toString();
        if (!owner.isCreative())
            consumeItem(owner, CommonModHelper.item(order.item), (int) order.amount);

        PacketDistributor.sendToPlayer(owner, new OrderResponsePacket(true, positionStr));
    }

    private static void consumeItem(ServerPlayer owner, Item item, int count) {
        Inventory inventory = owner.getInventory();

        int remaining = count;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
            if (remaining <= 0)
                break;
        }

        ItemStack offhand = owner.getOffhandItem();
        if (remaining > 0)
            if (!offhand.isEmpty() && offhand.is(item))
                offhand.shrink(Math.min(remaining, offhand.getCount()));

        owner.containerMenu.broadcastChanges();
    }

    private static void sell(Order order) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        ServerPlayer owner = server.getPlayerList().getPlayer(order.ownerUUID);
        if (owner == null)
            return;

        Position position = Position.get(order.ownerUUID, order.item, order.ticker);
        if (position == null)
            return;

        Position newPosition = Position.fromOrder(order);
        Position toSave = position.trim(newPosition);
        reward(position, toSave, owner, order);

        if (toSave != null) {
            if (toSave.worth() >= 1.0D)
                toSave.save();
            else
                toSave.delete();

            PacketDistributor.sendToPlayer(owner, new OrderResponsePacket(true, toSave.toString()));
        } else {
            Position toDelete = new Position(owner, get(position.getTicker()), new ItemStack(CommonModHelper.item(position.getItem())), 0.1, position.getPrice());
            PacketDistributor.sendToPlayer(owner, new OrderResponsePacket(true, toDelete.toString()));
            position.delete();
        }
    }

    private static void reward(Position position, Position toSave, ServerPlayer owner, Order order) {
        owner.level().getServer().execute(() -> {
            Item givenItem = CommonModHelper.item(position.getItem());
            int itemsToAdd = toSave != null ? (int)(position.getAmount() - toSave.getAmount()) : (int) position.getAmount();
            int availableSpace = CommonModHelper.getAvailableInventorySpace(owner, givenItem);
            WolfOfMinestreet.LOGGER.info("AVAILABLE SPACE: " + availableSpace);
            if (itemsToAdd <= availableSpace)
                CommonModHelper.addItemToPlayer(owner, givenItem, itemsToAdd);
            else {
                int leftover = itemsToAdd - availableSpace;
                CommonModHelper.addItemToPlayer(owner, givenItem, availableSpace);
                while (leftover > 0) {
                    int added = Math.min(leftover, 64);
                    ItemStack droppedStack = new ItemStack(givenItem, added);
                    BlockPos pos = owner.blockPosition().above();
                    ItemEntity itemEntity = new ItemEntity(owner.level(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, droppedStack);
                    itemEntity.setPickUpDelay(100);

                    leftover -= added;
                    owner.level().addFreshEntity(itemEntity);
                    WolfOfMinestreet.LOGGER.info("DROPPING ITEMS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        });
    }

    public static void savePositionFile(Path file, JsonObject root) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static Path getOrInitPositionPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return null;

        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        Path positionPath = worldPath.resolve("minestreet/positions");
        if (!Files.exists(positionPath)) {
            try {
                Files.createDirectories(positionPath);
            } catch (Exception e) {
                return null;
            }
        }

        return positionPath;
    }

    public static Path getOrInitPathFile(UUID ownerUUID) {
        Path positionPath = getOrInitPositionPath();
        if (positionPath == null)
            return null;

        Path positionFile = positionPath.resolve(ownerUUID.toString() + ".json");
        if (!Files.exists(positionFile)) {
            try {
                Files.createDirectories(positionFile.getParent());
                Files.createFile(positionFile);
                init(positionFile);
            } catch (IOException e) {
                return null;
            }
        }

        return positionFile;
    }

    private static void init(Path jsonFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(jsonFile.toFile())) {
            JsonArray emptyArray = new JsonArray();
            JsonObject emptyRoot = new JsonObject();
            JsonObject dividends = new JsonObject();
            emptyRoot.add(StockMarketKeys.POSITIONS, emptyArray);
            emptyRoot.add(StockMarketKeys.DIVIDENDS, dividends);
            gson.toJson(emptyRoot, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonArray positionsOf(ServerPlayer player) {
        Path positionFile = getOrInitPathFile(player.getUUID());
        JsonObject root = Position.getFileAsJsonObject(positionFile);
        JsonArray positions = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();
        return positions;
    }
}

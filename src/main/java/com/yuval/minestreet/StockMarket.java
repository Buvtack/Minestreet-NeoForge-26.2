package com.yuval.minestreet;

import com.google.gson.*;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public static final long CLEAN_INTERVAL = 300_000_000_000L;

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
        JsonObject indicators = raw.getAsJsonObject("chart").getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("indicators");
        dataToStore.add(StockMarketKeys.TICKER, nestedData.get(StockMarketKeys.TICKER));
        dataToStore.add(StockMarketKeys.CURRENCY, nestedData.get(StockMarketKeys.CURRENCY));
        dataToStore.add(StockMarketKeys.PRICE, nestedData.get(StockMarketKeys.PRICE));
        dataToStore.add(StockMarketKeys.CHART_PREVIOUS_CLOSE, nestedData.get(StockMarketKeys.CHART_PREVIOUS_CLOSE));
        dataToStore.add(StockMarketKeys.NAME, nestedData.get(StockMarketKeys.NAME));
        dataToStore.add(StockMarketKeys.FETCHED_TIME, JsonParser.parseString(Long.toString(System.nanoTime())));
        JsonArray volume = indicators.getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray(StockMarketKeys.VOLUME);
        dataToStore.addProperty(StockMarketKeys.VOLUME, getVolume(volume));
        return dataToStore;
    }

    private static int getVolume(JsonArray volume) {
        int volumeResult = 0;
        for (JsonElement element : volume) {
            try {
                volumeResult += element.getAsInt();
            } catch (Exception e) {}
        }
        return volumeResult;
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
        //storedStocks.clear();
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
            toSave.save();
            PacketDistributor.sendToPlayer(owner, new OrderResponsePacket(true, toSave.toString()));
        } else
            position.delete();
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
                Files.createFile(positionFile);
                init(positionFile);
            } catch (IOException e) {
                return null;
            }
            return null;
        }

        return positionFile;
    }

    private static void init(Path jsonFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(jsonFile.toFile())) {
            JsonArray emptyArray = new JsonArray();
            JsonObject emptyRoot = new JsonObject();
            emptyRoot.add(StockMarketKeys.POSITIONS, emptyArray);
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

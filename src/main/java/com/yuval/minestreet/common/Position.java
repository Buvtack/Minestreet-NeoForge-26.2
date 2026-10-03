package com.yuval.minestreet.common;

import com.google.gson.*;
import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class Position {

    private String ticker;
    private Identifier itemId;
    private double amount;
    private double price;
    private UUID ownerUUID;
    private long date;

    public Position(Player owner, JsonObject stock, ItemStack stack, double count, double price) {
        ticker = StockMarket.get(stock.get(StockMarketKeys.TICKER).getAsString()).getAsString();
        itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        this.amount = count;
        this.price = price;
        ownerUUID = owner.getUUID();
    }

    private Position(String ticker, Identifier itemId, double count, double price, UUID ownerUUID, long date) {
        this.ticker = ticker;
        this.itemId = itemId;
        this.amount = count;
        this.price = price;
        this.ownerUUID = ownerUUID;
        this.date = date;
    }

    public void save() {
        Path positionFile = StockMarket.getOrInitPathFile(ownerUUID);
        if (positionFile == null) {
            WolfOfMinestreet.LOGGER.error("Cannot save position for " + ownerUUID.toString() + " because 'server' is null!");
            return;
        }

        JsonObject root = getFileAsJsonObject(positionFile);
        JsonArray positions = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();

        int i;
        for (i = 0; i < positions.size(); i++) {
            JsonObject position = positions.get(i).getAsJsonObject();
            if (position.get(StockMarketKeys.ITEM).getAsString().equals(itemId.toString()) && position.get(StockMarketKeys.TICKER).getAsString().equals(ticker)) {
                if (amount == 0)
                    positions.remove(i);
                else
                    positions.set(i, toJsonObject());

                break;
            }
        }
        if (i >= positions.size() && amount > 0)
            positions.add(toJsonObject());

        toJson(positionFile, root);
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty(StockMarketKeys.TICKER, ticker);
        object.addProperty(StockMarketKeys.ITEM, itemId.toString());
        object.addProperty(StockMarketKeys.AMOUNT, amount);
        object.addProperty(StockMarketKeys.POSITION_PRICE, price);
        object.addProperty(StockMarketKeys.OWNER, ownerUUID.toString());
        object.addProperty(StockMarketKeys.DATE, date);
        return object;
    }

    public void toJson(Path positionFile, JsonObject root) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(positionFile.toFile())) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Position combine(Position position) {
        if (!itemId.toString().equals(position.itemId.toString()))
            WolfOfMinestreet.LOGGER.warn("Combining 2 positions of different item types!!!");
        if (!ticker.equals(position.ticker))
            WolfOfMinestreet.LOGGER.warn("Combining 2 positions of different tickers!!!");
        if (!ownerUUID.toString().equals(position.ownerUUID.toString()))
            WolfOfMinestreet.LOGGER.warn("Combining 2 positions of different owners!!!");

        double totalAmount = amount + position.amount;
        if (totalAmount == 0)
            return new Position(ticker, itemId, 0, 0, ownerUUID, date);

        double thisWeight = amount / totalAmount;
        double otherWeight = position.amount / totalAmount;
        double newPrice = price * thisWeight + position.price * otherWeight;
        return new Position(ticker, itemId, totalAmount, newPrice, ownerUUID, date);
    }

    public Position trim(Position position) {
        if (!itemId.toString().equals(position.itemId.toString()))
            WolfOfMinestreet.LOGGER.warn("Trimming 2 positions of different item types!!!");
        if (!ticker.equals(position.ticker))
            WolfOfMinestreet.LOGGER.warn("Trimming 2 positions of different tickers!!!");
        if (!ownerUUID.toString().equals(position.ownerUUID.toString()))
            WolfOfMinestreet.LOGGER.warn("Trimming 2 positions of different owners!!!");

        double newAmount = amount - position.amount;
        if (newAmount < 0)
            return null;

        return new Position(ticker, itemId, newAmount, price, ownerUUID, date);
    }

    public Position delete() {
        Path positionFile = StockMarket.getOrInitPathFile(ownerUUID);
        if (positionFile == null)
            return this;

        JsonObject root = getFileAsJsonObject(positionFile);
        JsonArray positions = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();
        for (int i = 0; i < positions.size(); i++) {
            JsonObject object = positions.get(i).getAsJsonObject();
            if (is(object))
                positions.remove(i);
        }
        StockMarket.savePositionFile(positionFile, root);

        return this;
    }

    public double pnlPercentage() {
        return pnlRatio() * 100.0D;
    }

    public double pnl() {
        double ratio = pnlRatio();
        return ratio * amount;
    }

    private double pnlRatio() {
        double stockPrice = Double.parseDouble(StockMarket.getPrice(ticker));
        if (price == 0) {
            if (stockPrice > price)
                return stockPrice;
            return 0.0D;
        }
        return stockPrice / price - 1;
    }

    public double worth() {
        return amount + pnl();
    }

    public String clientId() {
        return ticker + "-" + itemId.toString();
    }

    public String serverId() {
        return ticker + "-" + itemId.toString() + "-" + ownerUUID.toString();
    }

    public long date() {
        return date;
    }

    public boolean is(Position other) {
        return serverId().equals(other.serverId());
    }

    public boolean is(JsonObject other) {
        Position otherPos = fromJsonObject(other);
        return is(otherPos);
    }

    @Override
    public String toString() {
        return toJsonObject() != null ? toJsonObject().toString() : "null";
    }

    public double getAmount() {
        return amount;
    }

    public String getTicker() {
        return ticker;
    }

    public double getPrice() {
        return price;
    }

    public Identifier getItem() {
        return itemId;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public static Position get(UUID ownerUUID, Identifier item, String ticker) {
        Path positionFile = StockMarket.getOrInitPathFile(ownerUUID);
        if (positionFile == null)
            return null;

        JsonObject root = getFileAsJsonObject(positionFile);
        JsonArray positions = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();
        for (JsonElement element : positions) {
            JsonObject positionJson = element.getAsJsonObject();
            Position position = fromJsonObject(positionJson);
            if (position.itemId.toString().equals(item.toString()) && position.ticker.equals(ticker)) {
                return position;
            }
        }
        return null;
    }

    public static JsonObject getFileAsJsonObject(Path jsonFile) {
        try (FileReader reader = new FileReader(jsonFile.toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    public static Position fromJsonObject(JsonObject object) {
        String ticker = object.get(StockMarketKeys.TICKER).getAsString();
        Identifier itemId = Identifier.parse(object.get(StockMarketKeys.ITEM).getAsString());
        double amount = object.get(StockMarketKeys.AMOUNT).getAsDouble();
        double price = object.get(StockMarketKeys.POSITION_PRICE).getAsDouble();
        UUID uuid = UUID.fromString(object.get(StockMarketKeys.OWNER).getAsString());

        long today = Instant.now().getEpochSecond();
        long date = object.has(StockMarketKeys.DATE) ? object.get(StockMarketKeys.DATE).getAsLong() : today;
        return new Position(ticker, itemId, amount, price, uuid, date);
    }

    public static Position fromOrder(Order order) {
        String ticker = order.ticker;
        Identifier itemId = order.item;
        double amount = order.amount;
        double price = order.price;
        UUID ownerUUID = order.ownerUUID;
        long date = Instant.now().getEpochSecond();
        return new Position(ticker, itemId, amount, price, ownerUUID, date);
    }
}

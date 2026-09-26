package com.yuval.minestreet.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.StockMarketKeys;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class Order {

    public final String ticker;
    public final Identifier item;
    public final double amount;
    public final double price;
    public final UUID ownerUUID;
    public final Type type;

    public Order(String ticker, Identifier item, double amount, double price, UUID ownerUUID, Type type) {
        this.ticker = ticker;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.ownerUUID = ownerUUID;
        this.type = type;
    }

    public static Order parse(String orderJson) {
        JsonObject root = JsonParser.parseString(orderJson).getAsJsonObject();
        String ticker = root.get(StockMarketKeys.TICKER).getAsString();
        Identifier item = Identifier.parse(root.get(StockMarketKeys.ITEM).getAsString());
        int amount = root.get(StockMarketKeys.AMOUNT).getAsInt();
        int price = root.get(StockMarketKeys.ORDER_PRICE).getAsInt();
        UUID ownerUUID = UUID.fromString(root.get(StockMarketKeys.OWNER).getAsString());
        Type type = Type.values()[root.get(StockMarketKeys.ORDER_TYPE).getAsInt()];
        return new Order(ticker, item, amount, price, ownerUUID, type);
    }

    public String toJsonString() {
        JsonObject object = new JsonObject();
        object.addProperty(StockMarketKeys.TICKER, ticker);
        object.addProperty(StockMarketKeys.ITEM, item.toString());
        object.addProperty(StockMarketKeys.AMOUNT, amount);
        object.addProperty(StockMarketKeys.ORDER_PRICE, price);
        object.addProperty(StockMarketKeys.OWNER, ownerUUID.toString());
        object.addProperty(StockMarketKeys.ORDER_TYPE, type.ordinal());

        return object.toString();
    }

    public enum Type {
        BUY, SELL
    }
}

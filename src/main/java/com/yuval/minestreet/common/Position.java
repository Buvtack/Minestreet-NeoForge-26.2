package com.yuval.minestreet.common;

import com.google.gson.*;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class Position {

    private String ticker;
    private Identifier itemId;
    private double count;
    private double price;
    private UUID ownerUUID;

    public Position(Player owner, JsonObject stock, ItemStack stack, double count, double price) {
        ticker = StockMarket.get(stock.get(StockMarketKeys.TICKER).getAsString()).getAsString();
        itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        this.count = count;
        this.price = price;
        ownerUUID = owner.getUUID();
    }

    public void save() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Path worldPath = server.getWorldPath(LevelResource.ROOT);
            Path positionDir = worldPath.resolve("minestreet_positions");
            Path playerFile = positionDir.resolve(ownerUUID.toString() + ".json");

            try {
                Files.createDirectories(positionDir);
                JsonObject rootData;
                if (Files.exists(playerFile)) {
                    try (BufferedReader reader = Files.newBufferedReader(playerFile)) {
                        rootData = JsonParser.parseReader(reader).getAsJsonObject();
                    }
                } else {
                    rootData = new JsonObject();
                    rootData.addProperty("ownerUUID", ownerUUID.toString());
                    rootData.add("positions", new JsonArray());
                }

                JsonArray positions /*SWITCHING MY POSITIONS FOR YOUUUUUU*/ = rootData.getAsJsonArray("positions");
                JsonObject targetPosition = find(positions);

                if (targetPosition != null) {
                    targetPosition.addProperty("count", count);
                    targetPosition.addProperty("price", price); // needs to be AVERAGEEEEEE URGENT URGENT URGENT
                } else {
                    positions.add(create());
                }

                try (BufferedWriter writer = Files.newBufferedWriter(playerFile)) {
                    new Gson().toJson(rootData, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JsonObject find(JsonArray positions) {
        JsonObject targetPosition = null;
        for (JsonElement element : positions) {
            JsonObject object = element.getAsJsonObject();
            if (object.get("ticker").getAsString().equalsIgnoreCase(ticker) && object.get("item").getAsString().equals(itemId.toString())) {
                targetPosition = object;
                break;
            }
        }

        return targetPosition;
    }

    private JsonObject create() {
        JsonObject position = new JsonObject();
        position.addProperty("ticker", ticker);
        position.addProperty("item", itemId.toString());
        position.addProperty("count", count);
        position.addProperty("price", price);
        position.addProperty("ownerUUID", String.valueOf(ownerUUID));
        return position;
    }
}

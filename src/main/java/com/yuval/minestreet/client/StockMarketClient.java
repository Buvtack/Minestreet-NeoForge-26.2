package com.yuval.minestreet.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.components.StockEntry;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.common.Position;
import com.yuval.minestreet.network.packets.GetStockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockMarketClient {

    public static JsonObject get(String ticker) {
        ticker = ticker.toUpperCase().trim();
        if (!StockMarket.storedStocks.containsKey(ticker)) {
            ClientPacketDistributor.sendToServer(new GetStockPacket(ticker));
        }
        return StockMarket.storedStocks.get(ticker);
    }

    public static void clean() {
        WolfOfMinestreet.LOGGER.info("CLEANING");
        Set<String> keptTickers = new HashSet<>();

        for (String ticker : StockMarket.INITIAL_TICKERS) {
            get(ticker);
            keptTickers.add(ticker);
        }

        if (ServerLifecycleHooks.getCurrentServer() != null)
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                JsonObject root = Position.getFileAsJsonObject(StockMarket.getOrInitPathFile(player.getUUID()));
                JsonArray array = root.get(StockMarketKeys.POSITIONS).getAsJsonArray();
                for (JsonElement element : array) {
                    String ticker = element.getAsJsonObject().get(StockMarketKeys.TICKER).getAsString();
                    get(ticker);
                    keptTickers.add(ticker);
                }
            }

        if (ModHelper.screen() instanceof TradingStationScreen screen) {
            List<StockEntry> searchedStocks = new ArrayList<>(screen.getSearchedStocks());
            for (StockEntry entry : searchedStocks) {
                String ticker = entry.stock.get(StockMarketKeys.TICKER).getAsString();
                get(ticker);
                keptTickers.add(ticker);
            }

            screen.refresh();
        }

        StockMarket.storedStocks.keySet().removeIf(ticker -> !keptTickers.contains(ticker));
    }
}

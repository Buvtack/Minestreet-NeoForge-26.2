package com.yuval.minestreet.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.screens.TradingStationMenu;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record SyncStockPacket(String stocks) implements CustomPacketPayload {

    public static final Type<SyncStockPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "sync_stock")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncStockPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncStockPacket::stocks,
            SyncStockPacket::new
    );

    public static void handle(SyncStockPacket packet, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            CompletableFuture.runAsync(() -> {
                JsonObject jsonObject = JsonParser.parseString(packet.stocks).getAsJsonObject();
                JsonArray array = jsonObject.get("array").getAsJsonArray();
                array.forEach(element -> {
                    String ticker = element.getAsJsonObject().get(StockMarketKeys.TICKER).getAsString();
                    StockMarket.storedStocks.put(ticker, element.getAsJsonObject());
                });

                Screen screen = Minecraft.getInstance().gui.screen();
                if (screen instanceof TradingStationScreen tsScreen) {
                    tsScreen.updateStockEntryList();
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

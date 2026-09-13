package com.yuval.minestreet.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record InitStockPacket(String stocks) implements CustomPacketPayload {

    public static final Type<InitStockPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "init_stock")
    );

    public static final StreamCodec<FriendlyByteBuf, InitStockPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, InitStockPacket::stocks,
            InitStockPacket::new
    );

    public static void handle(InitStockPacket packet, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                JsonObject json = JsonParser.parseString(packet.stocks).getAsJsonObject();
                JsonArray array = json.getAsJsonArray("array");
                array.forEach(element -> {
                    JsonObject stock = element.getAsJsonObject();
                    String ticker = stock.get(StockMarketKeys.TICKER).getAsString();
                    //shit
                    StockMarket.storedStocks.put(ticker, stock);
                });

                Screen screen = Minecraft.getInstance().gui.screen();
                if (screen instanceof TradingStationScreen tsScreen)
                    tsScreen.updateStockEntryList();
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

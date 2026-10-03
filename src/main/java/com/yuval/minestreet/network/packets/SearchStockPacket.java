package com.yuval.minestreet.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.WolfOfMinestreet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record SearchStockPacket(String searched) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SearchStockPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "request_stock"));


    public static final StreamCodec<FriendlyByteBuf, SearchStockPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SearchStockPacket::searched,
            SearchStockPacket::new
    );

    public static void handle(SearchStockPacket packet, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            CompletableFuture.runAsync(() -> {
                StockMarket.searchAndStore(packet.searched);

                JsonObject result = new JsonObject();
                JsonArray array = new JsonArray();
                String searched = packet.searched.toLowerCase();
                for (String ticker : StockMarket.storedStocks.keySet()) {
                    JsonObject stock = StockMarket.storedStocks.get(ticker);
                    String stockName = StockMarket.name(stock).toLowerCase();

                    if (ticker.toLowerCase().contains(searched) || stockName.contains(searched))
                        array.add(StockMarket.storedStocks.get(ticker));
                }
                result.add("array", array);

                context.enqueueWork(() -> {
                    context.reply(new SyncStockPacket(result.toString()));
                });
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

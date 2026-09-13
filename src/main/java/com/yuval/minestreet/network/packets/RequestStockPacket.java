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

public record RequestStockPacket(String searched) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestStockPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "request_stock"));


    public static final StreamCodec<FriendlyByteBuf, RequestStockPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RequestStockPacket::searched,
            RequestStockPacket::new
    );

    public static void handle(RequestStockPacket packet, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            CompletableFuture.runAsync(() -> {
                StockMarket.searchAndStore(packet.searched);

                JsonObject result = new JsonObject();
                JsonArray array = new JsonArray();
                for (String ticker : StockMarket.storedStocks.keySet()) {
                    if (ticker.contains(packet.searched)) {
                        array.add(StockMarket.storedStocks.get(ticker));
                    }
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

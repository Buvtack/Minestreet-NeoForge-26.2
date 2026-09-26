package com.yuval.minestreet.network.packets;

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

public record GetStockPacket(String ticker) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GetStockPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "get_stock"));

    public static final StreamCodec<FriendlyByteBuf, GetStockPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GetStockPacket::ticker,
            GetStockPacket::new
    );

    public static void handle(GetStockPacket packet, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            CompletableFuture.runAsync(() -> {
                JsonObject stock = StockMarket.get(packet.ticker);
                String stockJson = stock.toString();
                context.reply(new GetStockResponsePacket(stockJson));
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

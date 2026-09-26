package com.yuval.minestreet.network.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GetStockResponsePacket(String stockJson) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GetStockResponsePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "get_stock_response"));

    public static final StreamCodec<FriendlyByteBuf, GetStockResponsePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GetStockResponsePacket::stockJson,
            GetStockResponsePacket::new
    );

    public static void handle(GetStockResponsePacket packet, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                JsonObject stock = JsonParser.parseString(packet.stockJson).getAsJsonObject();
                StockMarket.storedStocks.put(stock.get(StockMarketKeys.TICKER).getAsString(), stock);
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.yuval.minestreet.network.packets;

import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.common.Order;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SendOrderPacket(String order) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SendOrderPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "send_order"));


    public static final StreamCodec<FriendlyByteBuf, SendOrderPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SendOrderPacket::order,
            SendOrderPacket::new
    );

    public static void handle(SendOrderPacket packet, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                Order order = Order.parse(packet.order);
                WolfOfMinestreet.LOGGER.info("Received send order packet on Server: ");
                WolfOfMinestreet.LOGGER.info("Ticker: " + order.ticker + ", " + "Item: " + order.item.getPath());
                StockMarket.execute(order);
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

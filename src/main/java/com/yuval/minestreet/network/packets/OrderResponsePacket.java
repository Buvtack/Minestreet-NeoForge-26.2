package com.yuval.minestreet.network.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.Positions;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.common.Position;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OrderResponsePacket(boolean success, String position) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OrderResponsePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "order_response"));

    public static final StreamCodec<FriendlyByteBuf, OrderResponsePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OrderResponsePacket::success,
            ByteBufCodecs.STRING_UTF8, OrderResponsePacket::position,
            OrderResponsePacket::new
    );

    public static void handle(OrderResponsePacket packet, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            if (packet.success) {
                context.enqueueWork(() -> {
                    JsonObject object = JsonParser.parseString(packet.position).getAsJsonObject();
                    Position position = Position.fromJsonObject(object);
                    if (position.worth() >= 1.0D)
                        Positions.positions.put(position.clientId(), position);
                    else
                        Positions.positions.remove(position.clientId());

                    if (ModHelper.screen() instanceof TradingStationScreen screen) {
                        screen.updatePositionEntryList();
                        screen.updateInventory();
                    }
                });
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

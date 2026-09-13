package com.yuval.minestreet.network;

import com.yuval.minestreet.network.packets.InitStockPacket;
import com.yuval.minestreet.network.packets.RequestStockPacket;
import com.yuval.minestreet.network.packets.SyncStockPacket;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Packets {

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                RequestStockPacket.TYPE,
                RequestStockPacket.CODEC,
                RequestStockPacket::handle
        );

        registrar.playBidirectional(
                SyncStockPacket.TYPE,
                SyncStockPacket.CODEC,
                SyncStockPacket::handle
        );

        registrar.playBidirectional(
                InitStockPacket.TYPE,
                InitStockPacket.CODEC,
                InitStockPacket::handle
        );
    }

    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        event.register(
                RequestStockPacket.TYPE,
                RequestStockPacket::handle
        );

        event.register(
                SyncStockPacket.TYPE,
                SyncStockPacket::handle
        );

        event.register(
                InitStockPacket.TYPE,
                InitStockPacket::handle
        );
    }
}

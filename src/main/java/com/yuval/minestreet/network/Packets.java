package com.yuval.minestreet.network;

import com.yuval.minestreet.network.packets.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Packets {

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                SearchStockPacket.TYPE,
                SearchStockPacket.CODEC,
                SearchStockPacket::handle
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

        registrar.playBidirectional(
                OrderResponsePacket.TYPE,
                OrderResponsePacket.CODEC,
                OrderResponsePacket::handle
        );

        registrar.playBidirectional(
                SendOrderPacket.TYPE,
                SendOrderPacket.CODEC,
                SendOrderPacket::handle
        );

        registrar.playBidirectional(
                GetStockPacket.TYPE,
                GetStockPacket.CODEC,
                GetStockPacket::handle
        );

        registrar.playBidirectional(
                GetStockResponsePacket.TYPE,
                GetStockResponsePacket.CODEC,
                GetStockResponsePacket::handle
        );
    }

    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        event.register(
                SearchStockPacket.TYPE,
                SearchStockPacket::handle
        );

        event.register(
                SyncStockPacket.TYPE,
                SyncStockPacket::handle
        );

        event.register(
                InitStockPacket.TYPE,
                InitStockPacket::handle
        );

        event.register(
                OrderResponsePacket.TYPE,
                OrderResponsePacket::handle
        );

        event.register(
                SendOrderPacket.TYPE,
                SendOrderPacket::handle
        );

        event.register(
                GetStockPacket.TYPE,
                GetStockPacket::handle
        );

        event.register(
                GetStockResponsePacket.TYPE,
                GetStockResponsePacket::handle
        );
    }
}

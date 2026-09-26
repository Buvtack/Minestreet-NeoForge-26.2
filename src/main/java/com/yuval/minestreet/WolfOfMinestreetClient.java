package com.yuval.minestreet;

import com.yuval.minestreet.client.StockMarketClient;
import com.yuval.minestreet.network.Packets;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.concurrent.CompletableFuture;

@Mod(value = WolfOfMinestreet.MODID, dist = Dist.CLIENT)

@EventBusSubscriber(modid = WolfOfMinestreet.MODID, value = Dist.CLIENT)
public class WolfOfMinestreetClient {

    private static long time = System.nanoTime();

    public WolfOfMinestreetClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerPackets(RegisterClientPayloadHandlersEvent event) {
        Packets.registerClient(event);
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (System.nanoTime() - time >= StockMarket.CLEAN_INTERVAL && !Minecraft.getInstance().isPaused()) {
            time = System.nanoTime();
            CompletableFuture.runAsync(StockMarketClient::clean);
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        WolfOfMinestreet.LOGGER.info("HELLO FROM CLIENT SETUP");
        WolfOfMinestreet.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}

package com.yuval.minestreet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yuval.minestreet.blocks.ModBlockEntities;
import com.yuval.minestreet.blocks.ModBlocks;
import com.yuval.minestreet.blocks.TradingStationBlockEntity;
import com.yuval.minestreet.gui.ModCreativeTabs;
import com.yuval.minestreet.gui.ModMenus;
import com.yuval.minestreet.items.ModItems;
import com.yuval.minestreet.network.Packets;
import com.yuval.minestreet.network.packets.InitStockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Supplier;

@Mod(WolfOfMinestreet.MODID)
public class WolfOfMinestreet {

    public static final String MODID = "minestreet";

    public static final Logger LOGGER = LogUtils.getLogger();

    public WolfOfMinestreet(IEventBus modEventBus, ModContainer modContainer) {
        //NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(WolfOfMinestreet::onServerStarting);
        NeoForge.EVENT_BUS.addListener(WolfOfMinestreet::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(WolfOfMinestreet::tick);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeTabs);
        modEventBus.addListener(this::registerPackets);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
    }

    public void registerPackets(RegisterPayloadHandlersEvent event) {
        Packets.register(event.registrar("1"));
    }

    //@SubscribeEvent
    public void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.MOD_CREATIVE_TAB.getKey()) {
            event.accept(ModBlocks.TRADING_STATION.get());
        }  if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(ModBlocks.TRADING_STATION.get());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Fetching stock markets data... LET'S MAKE SOME EMERALDS!!!");
        LOGGER.info("What's the stock of the day... I know, SOXL! Price: " + StockMarket.getPrice("SOXL"));
    }

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        StockMarket.initialize();
    }

    @SubscribeEvent
    static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        JsonObject result = new JsonObject();
        JsonArray array = new JsonArray();
        for (String ticker : StockMarket.INITIAL_TICKERS) {
            JsonObject stock = StockMarket.get(ticker);
            array.add(stock);
        }

        result.add("array", array);

        ServerPlayer player = (ServerPlayer) event.getEntity();
        PacketDistributor.sendToPlayer(player, new InitStockPacket(result.toString()));
    }

    @SubscribeEvent
    static void tick(ServerTickEvent.Post event) {
        StockMarket.clean();
    }
}

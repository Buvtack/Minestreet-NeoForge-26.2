package com.yuval.minestreet;

import com.yuval.minestreet.blocks.ModBlockEntities;
import com.yuval.minestreet.blocks.ModBlocks;
import com.yuval.minestreet.blocks.TradingStationBlockEntity;
import com.yuval.minestreet.gui.ModCreativeTabs;
import com.yuval.minestreet.gui.ModMenus;
import com.yuval.minestreet.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
import java.util.function.Supplier;

@Mod(WolfOfMinestreet.MODID)
public class WolfOfMinestreet {

    public static final String MODID = "minestreet";

    public static final Logger LOGGER = LogUtils.getLogger();

    public WolfOfMinestreet(IEventBus modEventBus, ModContainer modContainer) {
        //NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeTabs);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
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
}

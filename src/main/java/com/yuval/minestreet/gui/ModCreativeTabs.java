package com.yuval.minestreet.gui;

import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.blocks.ModBlockEntities;
import com.yuval.minestreet.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WolfOfMinestreet.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_CREATIVE_TAB =
            CREATIVE_TABS.register("minestreet_creative_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Wolf Of Minestreet"))
                    .icon(() -> new ItemStack(ModBlocks.TRADING_STATION.get()))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}

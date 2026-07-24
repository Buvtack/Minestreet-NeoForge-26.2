package com.yuval.minestreet.gui;

import com.yuval.minestreet.client.gui.screens.TradingStationMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, "minestreet");

    public static final DeferredHolder<MenuType<?>, MenuType<TradingStationMenu>> TRADING_STATION_MENU =
            MENUS.register("trading_station",
                    () -> new MenuType<>(TradingStationMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}

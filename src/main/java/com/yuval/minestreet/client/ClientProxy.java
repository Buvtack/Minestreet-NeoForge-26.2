package com.yuval.minestreet.client;

import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.gui.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = WolfOfMinestreet.MODID, value = Dist.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRADING_STATION_MENU.get(), TradingStationScreen::new);
    }
}

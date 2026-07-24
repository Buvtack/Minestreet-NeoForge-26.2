package com.yuval.minestreet.items;

import com.yuval.minestreet.WolfOfMinestreet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WolfOfMinestreet.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

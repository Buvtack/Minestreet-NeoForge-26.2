package com.yuval.minestreet.blocks;

import com.yuval.minestreet.WolfOfMinestreet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WolfOfMinestreet.MODID);

    public static final Supplier<BlockEntityType<TradingStationBlockEntity>> TRADING_STATION =
            BLOCK_ENTITIES.register(
                    "trading_station",
                    () -> new BlockEntityType<>(
                            TradingStationBlockEntity::new,
                            ModBlocks.TRADING_STATION.value()
                    )
            );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}

package com.yuval.minestreet.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TradingStationBlockEntity extends BlockEntity {

    public TradingStationBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.TRADING_STATION.get(), worldPosition, blockState);
    }
}

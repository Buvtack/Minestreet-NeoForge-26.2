package com.yuval.minestreet.client.gui.screens;

import com.yuval.minestreet.gui.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class TradingStationMenu extends AbstractContainerMenu {

    private BlockPos pos;

    public TradingStationMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, BlockPos.ZERO);
    }

    public TradingStationMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(ModMenus.TRADING_STATION_MENU.get(), containerId);
        this.pos = pos;

        // Add inventory slots here...
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }

        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    public TradingStationMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

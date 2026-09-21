package com.yuval.minestreet.client;

import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ModHelper {

    public static Screen screen() {
        return Minecraft.getInstance().gui.screen();
    }

    public static boolean selectedStack() {
        TradingStationScreen screen = (TradingStationScreen) screen();
        return screen.getSelectedStack() != null && !screen.getSelectedStack().isEmpty();
    }

    public static int getItemCount(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        Inventory inventory = player.getInventory();
        int result = 0;
        for (ItemStack inventoryStack : inventory) {
            if (stack.is(inventoryStack.getItem()))
                result += inventoryStack.count();
        }
        return result;
    }
}

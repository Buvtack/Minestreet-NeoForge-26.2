package com.yuval.minestreet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class CommonModHelper {

    public static boolean isNotLeftClicking() {
        long windowHandle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE;
    }

    public static boolean isLeftClicking() {
        long windowHandle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    @Deprecated
    public static float grow() {
        float guiScale = Minecraft.getInstance().options.guiScale().get();
        int sub = (int) (guiScale / 2);
        return guiScale / Math.max(guiScale - sub, 1);
    }

    @Deprecated
    public static float shrink() {
        float guiScale = Minecraft.getInstance().options.guiScale().get();
        int sub = (int) (guiScale / 2);
        return Math.max(guiScale - sub, 1) / guiScale;
    }

    public static ServerPlayer player(UUID uuid) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            if (player.getUUID().equals(uuid))
                return player;

        return null;
    }

    public static Item item(Identifier id) {
        return BuiltInRegistries.ITEM.getValue(id);
    }

    public static int getItemCount(Player player, Item item) {
        int total = 0;

        for (ItemStack stack : player.getInventory())
            if (!stack.isEmpty() && stack.is(item))
                total += stack.getCount();

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.is(item))
            total += offhand.getCount();

        return total;
    }

    public static int getItemCount(Player player, Identifier itemId) {
        Item item = item(itemId);
        return getItemCount(player, item);
    }

    public static int getAvailableInventorySpace(ServerPlayer player, Item item) {
        Inventory inventory = player.getInventory();
        int maxStackSize = item.getDefaultMaxStackSize();
        int result = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty())
                result += maxStackSize;
            else if (stack.is(item))
                result += (maxStackSize - stack.getCount());
        }

        return result;
    }

    public static void addItemToPlayer(ServerPlayer player, Item item, int amount) {
        player.getInventory().add(new ItemStack(item, amount));
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }
}

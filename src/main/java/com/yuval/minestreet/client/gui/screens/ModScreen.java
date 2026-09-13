package com.yuval.minestreet.client.gui.screens;

import com.yuval.minestreet.client.ModMouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class ModScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected GuiGraphicsExtractor graphics;
    protected int mouseX;
    protected int mouseY;
    protected float partialTick;

    public ModScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;

        ModMouseHandler.mouseX = mouseX;
        ModMouseHandler.mouseY = mouseY;
    }
}

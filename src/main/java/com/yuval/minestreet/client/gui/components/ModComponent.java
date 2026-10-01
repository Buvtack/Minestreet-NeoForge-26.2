package com.yuval.minestreet.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Map;

public abstract class ModComponent {

    protected GuiGraphicsExtractor graphics;
    protected Font font;

    protected int x;
    protected int y;
    protected int mouseX;
    protected int mouseY;
    protected float partialTick;

    protected int fadeTime;
    protected int fade;

    //private Map<>

    public ModComponent(int x, int y) {
        font = Minecraft.getInstance().font;
        this.x = x;
        this.y = y;
    }

    public abstract void tick();

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        doRender();
    }

    public abstract void doRender();

    public void fade(int fadeTime) {
        this.fadeTime = fadeTime;
    }
}

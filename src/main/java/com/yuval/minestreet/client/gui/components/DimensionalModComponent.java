package com.yuval.minestreet.client.gui.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class DimensionalModComponent extends ModComponent {

    protected int width;
    protected int height;

    public float fadeTicks;
    private float fade;

    public DimensionalModComponent(int x, int y, int width, int height, float fadeTicks) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.fadeTicks = fadeTicks;
        fade = 0;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        tickFade(mouseX, mouseY, partialTick);
    }

    private void tickFade(int mouseX, int mouseY, float partialTick) {
        if (isMouseOver(mouseX, mouseY) && fade < fadeTicks)
            fade += partialTick;
        if (!isMouseOver(mouseX, mouseY))
            fade = 0;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    protected float fade() {
        return fade / fadeTicks;
    }
}

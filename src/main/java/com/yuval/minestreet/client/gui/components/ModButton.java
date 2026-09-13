package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.ColorHelper;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class ModButton extends DimensionalModComponent {

    private Runnable onClick;

    private ModLabel text;
    private int backgroundColor;
    private int textColor;
    private TradingPanel panel;

    private boolean clickable = false;

    public ModButton(int x, int y, int width, Runnable onClick) {
        super(x, y, Math.max(width, 20), 20, 3);
        this.onClick = onClick;
    }

    public ModButton(int x, int y, int width, Runnable onClick, ModLabel text, int backgroundColor, int textColor) {
        this(x, y, width, onClick);
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    private ModButton(int x, int y, int width, String text, ModLabel.Alignment alignment) {
        super(x, y, width, 20, 3);
        int labelX = alignment == ModLabel.Alignment.CENTER ? x + width / 2 : (alignment == ModLabel.Alignment.RIGHT ? x - width : x);
        ModLabel label = new ModLabel(labelX, y + height / 3, Component.translatable(text), 0xFFFFFFFF, alignment);
        this.text = label;
    }

    public static ModButton builder(int x, int y, int width, Component text, ModLabel.Alignment alignment) {
        return new ModButton(x, y, width, text.getString(), alignment);
    }

    public ModButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public ModButton color(int color) {
        backgroundColor = color;
        return this;
    }

    public ModButton panel(TradingPanel panel) {
        this.panel = panel;
        return this;
    }

    public ModButton build() {
        return this;
    }

    @Override
    public void tick() {
        if (CommonModHelper.isNotLeftClicking())
            clickable = true;

        if (isMouseOver() && clickable && CommonModHelper.isLeftClicking()) {
            clickable = false;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F)
            );

            onClick.run();
        }
    }

    private boolean isMouseOver() {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void doRender() {
        Identifier buttonTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/button.png");

        int halfWidth = width / 2;
        int remainder = width % 2;

        graphics.blit(buttonTexture, x, y, x + halfWidth + remainder, y + height, 0.0f, (float) (halfWidth + remainder) / 200.0f, 0.0f, 1.0f);
        graphics.blit(buttonTexture, x + halfWidth + remainder, y, x + width, y + height, 1.0f - ((float) halfWidth / 200.0f), 1.0f, 0.0f, 1.0f);

        //if (isMouseOver())
        boolean selected = panel.getSelectedButton() == this;
        int color = ColorHelper.lerpColor(fade(), backgroundColor, backgroundColor + ModColors.EXTRA_ALPHA);
        color = selected ? backgroundColor + ModColors.EXTRA_ALPHA + 0x11000000 : color;
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
        if (selected)
            graphics.outline(x, y, width, height, backgroundColor + ModColors.EXTRA_ALPHA);

        text.render(graphics, mouseX, mouseY, partialTick);
    }

    public void setText(ModLabel text) {
        this.text = text;
    }
}

package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.client.gui.ModColor;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class ModButton extends DimensionalModComponent {

    private Runnable onClick;

    private ModLabel text;
    private ModColor backgroundColor;
    private int textColor;
    private TradingPanel panel;
    private ModTooltip tooltip = null;
    private ModIcon icon = null;

    private boolean clickable = false;

    public ModButton(int x, int y, int width, Runnable onClick) {
        super(x, y, Math.max(width, 20), 20, 3);
        this.onClick = onClick;
    }

    public ModButton(int x, int y, int width, Runnable onClick, ModLabel text, ModColor backgroundColor, int textColor) {
        this(x, y, width, onClick);
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    private ModButton(int x, int y, int width, String text, ModLabel.Alignment alignment) {
        super(x, y, width, 20, 3);
        int labelX = alignment == ModLabel.Alignment.CENTER ? x + width / 2 : (alignment == ModLabel.Alignment.RIGHT ? x - width : x);
        ModLabel label = new ModLabel(labelX, y + height / 3, Component.translatable(text), ModColors.WHITE, alignment);
        this.text = label;
    }

    public static ModButton builder(int x, int y, int width, Component text, ModLabel.Alignment alignment) {
        return new ModButton(x, y, width, text.getString(), alignment);
    }

    public ModButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public ModButton color(ModColor color) {
        backgroundColor = color;
        return this;
    }

    public ModButton panel(TradingPanel panel) {
        this.panel = panel;
        return this;
    }

    public ModButton tooltip(ModTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public ModButton icon(ModIcon icon) {
        this.icon = icon;
        return this;
    }

    public ModButton build() {
        return this;
    }

    @Override
    public void doTick() {
        if (CommonModHelper.isNotLeftClicking())
            clickable = true;

        if (isMouseOver() && clickable && CommonModHelper.isLeftClicking()) {
            clickable = false;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F)
            );

            if (onClick != null)
                onClick.run();
        }

        if (tooltip != null)
            tooltip.tick();
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

        boolean selected = panel != null && (panel.getSelectedButton() == this || panel.getSelectedInput() == this);
        ModColor color = backgroundColor.alphaify(fade() / 3F);
        color = selected ? color.alphaify(0.4F) : color;
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color.color);
        if (selected)
            graphics.outline(x, y, width, height, backgroundColor.alphaify(0.1F).color);

        text.render(graphics, mouseX, mouseY, partialTick);

        if (tooltip != null && isMouseOver(mouseX, mouseY)) {
            tooltip.render(graphics, mouseX, mouseY, partialTick);
        }

        if (icon != null)
            icon.render(graphics, mouseX, mouseY, partialTick);
    }

    public void setText(ModLabel text) {
        this.text = text;
    }
}

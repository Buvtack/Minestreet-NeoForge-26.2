package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.client.gui.ModColor;
import net.minecraft.network.chat.Component;

public class ModLabel extends ModComponent {

    public Component content;
    public ModColor color;

    private int width;
    private int height;
    private Alignment alignment;

    public ModLabel(int x, int y, Component content, ModColor color, Alignment alignment) {
        super(x, y);
        this.content = content;
        this.color = color;
        this.alignment = alignment;

        height = font.lineHeight;
        width = font.width(content.getString());
    }

    @Override
    public void doTick() {

    }

    @Override
    public void doRender() {
        int textX = alignment == Alignment.CENTER ? x - width / 2 : (alignment == Alignment.RIGHT ? x - width : x);
        graphics.text(font, content, textX, y, color.color);
    }

    public void setContent(Component content) {
        this.content = content;
        this.width = font.width(content.getString());
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
}

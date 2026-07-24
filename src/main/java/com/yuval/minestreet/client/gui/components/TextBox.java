package com.yuval.minestreet.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TextBox extends EditBox {


    public TextBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
    }

    public void init() {
        setMaxLength(128);
        setBordered(true);
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (isFocused())
            graphics.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, 0xFF34E4EA);
    }
}

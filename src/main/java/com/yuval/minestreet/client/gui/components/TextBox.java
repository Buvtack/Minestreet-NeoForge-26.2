package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.client.gui.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextBox extends EditBox {

    public static short typingTime = 20;
    public short typedFor = 0;
    public boolean finishedTyping = false;

    private ModLabel placeholder;

    public TextBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
        placeholder = new ModLabel(x + 5, y + height / 2 - Minecraft.getInstance().font.lineHeight / 2, narration, new ModColor(0xAAAAAAAA), ModLabel.Alignment.LEFT);
    }

    public void init() {
        setMaxLength(128);
        setBordered(true);
    }

    public void tick() {
        if (typedFor < typingTime) {
            typedFor++;
            if (typedFor == typingTime)
                finishedTyping = true;
        }
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (isFocused())
            graphics.outline(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0xFF34E4EA);

        if (getValue().isEmpty()) {
            placeholder.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                setFocused(false);
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        typedFor = 0;
        finishedTyping = false;
        return super.charTyped(event);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }
}

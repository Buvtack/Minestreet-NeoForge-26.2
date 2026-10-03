package com.yuval.minestreet.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.HashMap;
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

    private Map<String, Runnable> tickTasks;
    private Map<String, Runnable> renderTasks;

    public ModComponent(int x, int y) {
        font = Minecraft.getInstance().font;
        this.x = x;
        this.y = y;
        tickTasks = new HashMap<>();
        renderTasks = new HashMap<>();
    }

    public void addTickTask(String name, Runnable task) {
        tickTasks.put(name, task);
    }

    public Runnable removeTickTask(String name) {
        return tickTasks.remove(name);
    }

    public void addRenderTask(String name, Runnable task) {
        renderTasks.put(name, task);
    }

    public Runnable removeRenderTask(String name) {
        return renderTasks.remove(name);
    }

    public final void tick() {
        doTick();
        tickTasks.forEach((key, task) -> {
            task.run();
        });
    }

    public abstract void doTick();

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        doRender();
        renderTasks.forEach((key, task) -> {
            task.run();
        });
    }

    public abstract void doRender();

    public void fade(int fadeTime) {
        this.fadeTime = fadeTime;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    public boolean isFocused() {
        return false;
    }

    public void setFocused(boolean focused) {}

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }
}

package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.gui.ColorHelper;
import com.yuval.minestreet.client.gui.ModColor;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class ModTooltip extends ModComponent implements Collection<ModLabel> {

    private List<ModLabel> lines;

    private int lastY;
    private int width;
    private ModColor color;
    private boolean changed;

    private final int MARGIN = 1;

    public ModTooltip(int x, int y, int width, ModColor color) {
        super(x, y);
        lines = new LinkedList<>();
        lastY = y;
        this.width = width;
        this.color = color;
        changed = false;
    }

    public ModTooltip(int x, int y, int width, ModColor color, Component content) {
        this(x, y, width, color);
        List<String> splitContent = ModHelper.split(content.getString(), width);
        for (String line : splitContent) {
            ModLabel label = buildLabel(lastY, Component.literal(line), ModColors.WHITE, ModLabel.Alignment.RIGHT);
            add(label);
        }

        changed = true;
    }

    @Override
    public void doTick() {
        if (changed) {
            lastY = y - MARGIN;
            for (ModLabel label : this) {
                label.y = lastY - height();
                lastY += font.lineHeight;
            }

            changed = false;
        }
    }

    @Override
    public void doRender() {
        graphics.nextStratum();

        ModColor alphadColor = ModColors.STOCK_LIST_COLOR.alphaify(0.95F).saturate(0.25F);
        int backgroundColor = alphadColor.darken(0.25F).color;
        int backgroundColor2 = alphadColor.greenify(1F).deblueify(0.1F).darken(0.3F).color;

        graphics.fillGradient(x - MARGIN, y - MARGIN * 2 - height(), x + font.width(getLongestLine()) + MARGIN, y, backgroundColor, backgroundColor2);
        graphics.outline(x - MARGIN - 1, y - MARGIN * 2 - height() - 1, font.width(getLongestLine()) + MARGIN + 3, height() + MARGIN * 2 + 2, color.darken(0.5F).alphaify(0.5F).color);
        lines.forEach(line -> line.render(graphics, mouseX, mouseY, partialTick));
    }

    private int getLongestLineWidth() {
        return getLongestLine().length();
    }

    private String getLongestLine() {
        String longest = "";
        for (ModLabel label : this) {
            if (label.content.getString().length() > longest.length())
                longest = label.content.getString();
        }
        return longest;
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return lines.contains(o);
    }

    @Override
    public @NotNull Iterator<ModLabel> iterator() {
        return lines.iterator();
    }

    @Override
    public @NotNull Object[] toArray() {
        return lines.toArray(ModLabel[]::new);
    }

    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        return null;
    }

    public void addLine(Component line, ModColor color, ModLabel.Alignment alignment) {
        ModLabel label = buildLabel(lastY, line, color, alignment);
        lines.add(label);
        lastY += font.lineHeight;
        changed = true;
    }

    public void addLineAt(int index, Component line, ModColor color, ModLabel.Alignment alignment) {
        ModLabel label = buildLabel(y + font.lineHeight * index, line, color, alignment);
        lines.add(index, label);
        moveForward(index);
        lastY += font.lineHeight;
        changed = true;
    }

    private void moveForward(int index) {
        for (int i = index + 1; i < lines.size(); i++) {
            ModLabel current = lines.get(i);
            current.y = y + font.lineHeight * (i + 1);
        }
    }

    private void moveBackward(int index) {
        for (int i = index; i < lines.size(); i++) {
            ModLabel current = lines.get(i);
            current.y = y + font.lineHeight * i;
        }
    }

    private ModLabel buildLabel(int y, Component line, ModColor color, ModLabel.Alignment alignment) {
        return new ModLabel(adjustX(line, alignment), y, line, color, alignment);
    }

    @Override
    public boolean add(ModLabel modLabel) {
        lastY += font.lineHeight;
        changed = true;
        return lines.add(modLabel);
    }

    private int adjustX(Component line, ModLabel.Alignment alignment) {
        int textWidth = font.width(line.getString());
        return alignment == ModLabel.Alignment.CENTER ? x + textWidth / 2 : (alignment == ModLabel.Alignment.RIGHT ? x + textWidth : x);
    }

    @Override
    public boolean remove(Object o) {
        lastY -= font.lineHeight;
        changed = true;
        return lines.remove(o);
    }

    public boolean remove(ModLabel label) {
        lastY -= font.lineHeight;
        changed = true;
        return lines.remove(label);
    }

    public ModLabel removeAt(int index) {
        lastY -= font.lineHeight;
        ModLabel toReturn = lines.remove(index);
        moveBackward(index);
        changed = true;
        return toReturn;
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super ModLabel> filter) {
        if (lines.removeIf(filter)) {
            lastY -= font.lineHeight;
            changed = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return lines.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ModLabel> c) {
        lastY += (font.lineHeight * c.size());
        changed = true;
        return lines.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        lastY -= (font.lineHeight * c.size());
        changed = true;
        return lines.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (lines.retainAll(c)) {
            lastY -= font.lineHeight * lines.size();
            changed = true;
            return true;
        }

        return false;
    }

    @Override
    public void clear() {
        lastY = y;
        changed = true;
        lines.clear();
    }

    public int height() {
        return font.lineHeight * size();
    }

    public int width() {
        return font.width(getLongestLine());
    }
}

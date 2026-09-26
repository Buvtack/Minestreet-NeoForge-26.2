package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public abstract class ModEntry extends DimensionalModComponent {

    public static int WIDTH = 110;
    public static int HEIGHT = 10;

    protected boolean clickable = false;

    public ModEntry(int x, int y) {
        super(x, y, WIDTH, HEIGHT, 3);
    }

    public abstract void refresh();

    protected class Section {
        private int x;
        private int y;
        private int width;
        private int height;
        private String content;
        private double value;

        public Section(String content, int x, int y, int width, int height) {
            this.content = content;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            value = 0;
        }

        public Section(double value, String additional, int x, int y, int width, int height) {
            this.value = value;
            content = ModHelper.format(value) + additional;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Section(double value, int x, int y, int width, int height) {
            this(value, "", x, y, width, height);
        }

        public void render(int color) {
            Identifier smallFont = Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "minecraft_regular");
            Style smallFontStyle = Style.EMPTY.withFont(new FontDescription.Resource(smallFont));
            graphics.text(
                    font,
                    Component.literal(content).withStyle(style -> style.withFont(smallFontStyle.getFont())),
                    x, y + height / 2 - font.lineHeight / 2 - 1,
                    color,
                    false
            );
        }

        protected void refresh(double value, String additional) {
            this.value = value;
            content = ModHelper.format(value) + additional;
        }

        protected void refresh(double value) {
            this.value = value;
            content = ModHelper.format(value);
        }

        protected void refresh(String content) {
            value = 0;
            this.content = content;
        }

        public int getColor() {
            return value >= 0 ? (value > 0 ? 0xFF08EE81 : 0xFFFFFFFF) : 0xFFF23645;
        }
    }
}

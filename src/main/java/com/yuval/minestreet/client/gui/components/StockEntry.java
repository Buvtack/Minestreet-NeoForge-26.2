package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModMouseHandler;
import com.yuval.minestreet.client.gui.ColorHelper;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class StockEntry extends DimensionalModComponent {

    private Font font;

    public static final int WIDTH = 110;
    public static final int HEIGHT = 10;

    public JsonObject stock;



    private Section ticker;
    private Section price;
    private Section change;
    private Section percentage;

    private boolean clickable = false;

    public StockEntry(int x, int y, JsonObject stock) {
        super(x, y, WIDTH, HEIGHT, 3);
        this.stock = stock;

        String tickerStr = stock.get(StockMarketKeys.TICKER).getAsString();
        ticker = new Section(tickerStr, x + 5, y, 20, HEIGHT);
        price = new Section(stock.get(StockMarketKeys.PRICE).getAsString(), x + WIDTH / 4, y, 20, HEIGHT);
        change = new Section(StockMarket.getChange(tickerStr), x + WIDTH / 2, y, 20, HEIGHT);
        percentage = new Section(StockMarket.getChangePercentage(tickerStr) + "%", x + WIDTH * 3 / 4, y, 20, HEIGHT);
    }

    public void tick() {
        if (CommonModHelper.isNotLeftClicking())
            clickable = true;

        int mouseX = ModMouseHandler.mouseX;
        int mouseY = ModMouseHandler.mouseY;

        if (CommonModHelper.isLeftClicking() && clickable) {

            if (Minecraft.getInstance().gui.screen() instanceof TradingStationScreen screen && isMouseOver(mouseX, mouseY)) {
                screen.setSelectedStock(this);
            }
            clickable = false;
        }
    }

    @Override
    public void doRender() {
        //tickFade(mouseX, mouseY, partialTick);

        graphics.pose().pushMatrix();

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, getBackgroundColor());
        font = Minecraft.getInstance().font;

        ticker.render(0xFFFFFFFF);
        price.render(0xFFFFFFFF);
        change.render(change.getColor());
        percentage.render(change.getColor());
        graphics.pose().popMatrix();
    }

    private int getBackgroundColor() {
        int baseColor = ColorHelper.lerpColor(fade(), ModColors.STOCK_LIST_COLOR, ModColors.STOCK_LIST_COLOR + 0x33000000);
        int selectedColor = ModColors.STOCK_LIST_COLOR + 0x88000000;
        if (Minecraft.getInstance().gui.screen() instanceof TradingStationScreen screen)
            return this == screen.getSelectedStock() ? selectedColor : baseColor;

        return baseColor;
    }



    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private class Section {
        private int x;
        private int y;
        private int width;
        private int height;
        private String content;

        public Section(String content, int x, int y, int width, int height) {
            this.content = content;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
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

        public int getColor() {
            float contentf = Float.parseFloat(content);
            return contentf >= 0 ? (contentf > 0 ? 0xFF08EE81 : 0xFFFFFFFF) : 0xFFF23645;
        }
    }
}

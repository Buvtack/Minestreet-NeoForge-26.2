package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModMouseHandler;
import com.yuval.minestreet.client.StockMarketClient;
import com.yuval.minestreet.client.gui.ColorHelper;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class StockEntry extends ModEntry {

    private Font font;

    public static final int WIDTH = 110;
    public static final int HEIGHT = 10;

    public JsonObject stock;

    private Section ticker;
    private Section price;
    private Section change;
    private Section percentage;

    public StockEntry(int x, int y, JsonObject stock) {
        super(x, y);
        this.stock = stock;

        String tickerStr = stock.get(StockMarketKeys.TICKER).getAsString();
        ticker = new Section(tickerStr, x + 5, y, 20, HEIGHT);
        price = new Section(stock.get(StockMarketKeys.PRICE).getAsString(), x + WIDTH / 4, y, 20, HEIGHT);
        change = new Section(Double.parseDouble(StockMarket.getChange(tickerStr)), x + WIDTH / 2, y, 20, HEIGHT);
        percentage = new Section(Double.parseDouble(StockMarket.getChangePercentage(tickerStr)), "%", x + WIDTH * 3 / 4, y, 20, HEIGHT);
    }

    public void doTick() {
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
    public void refresh() {
        String tickerStr = stock.get(StockMarketKeys.TICKER).getAsString();
        JsonObject updatedStock = StockMarketClient.get(tickerStr);
        price.refresh(updatedStock.get(StockMarketKeys.PRICE).getAsDouble());
        change.refresh(Double.parseDouble(StockMarket.getChange(tickerStr)));
        percentage.refresh(Double.parseDouble(StockMarket.getChangePercentage(tickerStr)), "%");
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
        int baseColor = ColorHelper.lerpColor(fade(), ModColors.STOCK_LIST_COLOR.color, ModColors.STOCK_LIST_COLOR.color + 0x33000000);
        int selectedColor = ModColors.STOCK_LIST_COLOR.color + 0x88000000;
        if (Minecraft.getInstance().gui.screen() instanceof TradingStationScreen screen)
            return this == screen.getSelectedStock() ? selectedColor : baseColor;

        return baseColor;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    @Override
    public String getTicker() {
        return stock.get(StockMarketKeys.TICKER).getAsString();
    }
}

package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.common.Position;
import net.minecraft.network.chat.Component;

public class ExtendedStockInfo extends ModComponent {

    private ModLabel volume;
    private ModLabel price;
    private ModLabel change;
    private ModLabel changePercentage;
    private ModLabel divYield;
    private PastReturns pastReturns;

    private JsonObject stock;

    public ExtendedStockInfo(int x, int y) {
        this(x, y, (JsonObject) null);
    }

    public ExtendedStockInfo(int x, int y, JsonObject stock) {
        super(x, y);
        setStock(stock);

        int gap = 2;
        volume = new ModLabel(x, y, Component.translatable(TranslationKeys.VOLUME), ModColors.WHITE, ModLabel.Alignment.LEFT);
        price = new ModLabel(x, volume.y + font.lineHeight + gap, Component.translatable(TranslationKeys.PRICE), ModColors.WHITE, ModLabel.Alignment.LEFT);
        change = new ModLabel(x, price.y + font.lineHeight + gap, Component.translatable(TranslationKeys.CHANGE), ModColors.WHITE, ModLabel.Alignment.LEFT);
        changePercentage = new ModLabel(x, change.y + font.lineHeight + gap, Component.translatable(TranslationKeys.CHANGE_PERCENTAGE), ModColors.WHITE, ModLabel.Alignment.LEFT);
        divYield = new ModLabel(x, changePercentage.y + font.lineHeight + gap, Component.translatable(TranslationKeys.DIVIDEND_YIELD), ModColors.WHITE, ModLabel.Alignment.LEFT);
    }

    public ExtendedStockInfo(int x, int y, Position position) {
        this(x, y, StockMarket.get(position.getTicker()));
    }

    public void setStock(JsonObject stock) {
        if (stock == null)
            return;

        this.stock = stock;

        String ticker = stock.get(StockMarketKeys.TICKER).getAsString();
        long volumeValue = stock.get(StockMarketKeys.VOLUME).getAsLong();
        double priceValue = stock.get(StockMarketKeys.PRICE).getAsDouble();
        double changeValue = Double.parseDouble(StockMarket.getChange(ticker));
        double changePercentageValue = Double.parseDouble(StockMarket.getChangePercentage(ticker));
        double divYieldValue = stock.get(StockMarketKeys.DIVIDEND_YIELD).getAsDouble();
        Component volumeContent = Component.translatable(TranslationKeys.VOLUME).append(ModHelper.format(volumeValue));
        Component priceContent = Component.translatable(TranslationKeys.PRICE).append(ModHelper.format(priceValue));
        Component changeContent = Component.translatable(TranslationKeys.CHANGE).append(ModHelper.format(changeValue)).withColor(ModHelper.getColor(changeValue).color);
        Component changePercentageContent = Component.translatable(TranslationKeys.CHANGE_PERCENTAGE).append(ModHelper.format(changePercentageValue)).append("%").withColor(ModHelper.getColor(changePercentageValue).color);
        Component divYieldContent = Component.translatable(TranslationKeys.DIVIDEND_YIELD).append(ModHelper.format(divYieldValue)).append("%");
        volume.setContent(volumeContent);
        price.setContent(priceContent);
        change.setContent(changeContent);
        changePercentage.setContent(changePercentageContent);
        divYield.setContent(divYieldContent);
    }

    public void setStock(Position position) {
        setStock(StockMarket.get(position.getTicker()));
    }

    @Override
    public void doTick() {
        if (stock == null)
            return;

        volume.doTick();
        price.doTick();
        change.doTick();
        changePercentage.doTick();
        divYield.doTick();
    }

    @Override
    public void doRender() {
        if (stock == null)
            return;

        volume.render(graphics, mouseX, mouseY, partialTick);
        price.render(graphics, mouseX, mouseY, partialTick);
        change.render(graphics, mouseX, mouseY, partialTick);
        changePercentage.render(graphics, mouseX, mouseY, partialTick);
        divYield.render(graphics, mouseX, mouseY, partialTick);
    }

    public JsonObject getStock() {
        return stock;
    }

    private static class PastReturns extends ModComponent {

        private ModLabel week;
        private ModLabel month;
        private ModLabel quarter;
        private ModLabel half;
        private ModLabel ytd;
        private ModLabel year;
        private ModLabel fiveYear;
        private ModLabel allTime;

        public PastReturns(int x, int y, JsonObject stock) {
            super(x, y);
        }

        public PastReturns(int x, int y, Position position) {
            super(x, y);
        }

        @Override
        public void doTick() {

        }

        @Override
        public void doRender() {

        }
    }
}

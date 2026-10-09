package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FundamentalsInfoPanel extends DimensionalModComponent {

    public static final int WIDTH = 228;
    public static final int HEIGHT = 168;

    private JsonObject stock;

    private ModLabel title;
    private ModLabel assetType;
    private ModLabel price;
    private ModLabel marketCap;
    private ModLabel change;
    private ModLabel changePercentage;
    private ModLabel dividendYield;
    private ModLabel peRatio;
    private ModLabel fiftyTwoWeekHigh;
    private ModLabel fiftyTwoWeekLow;

    private List<ModLabel> labels;

    private ModLabel pastReturnsTitle;
    private PastReturnsSection pastReturns;

    public FundamentalsInfoPanel(int x, int y) {
        super(x, y, WIDTH, HEIGHT, 3);

        labels = new ArrayList<>();
        pastReturnsTitle = new ModLabel(x + 5, height - 75, Component.translatable(TranslationKeys.PAST_RETURNS), ModColors.WHITE, ModLabel.Alignment.LEFT);
        pastReturns = new PastReturnsSection(x + width / 2 - 94, height - 70, 188, 80);

        getStock();
        refresh();
    }

    @Override
    public void doTick() {
        for (ModLabel label : labels)
            label.tick();

        pastReturnsTitle.tick();
        pastReturns.tick();
    }

    @Override
    public void doRender() {
        if (stock == null) {
            return;
        }

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, ModColors.STOCK_LIST_COLOR.color);
        for (ModLabel label : labels)
            label.render(graphics, mouseX, mouseY, partialTick);

        pastReturnsTitle.render(graphics, mouseX, mouseY, partialTick);
        pastReturns.render(graphics, mouseX, mouseY, partialTick);
    }

    private void reset() {
        title = null;
        assetType = null;
        price = null;
        marketCap = null;
        change = null;
        changePercentage = null;
        dividendYield = null;
        peRatio = null;
        fiftyTwoWeekHigh = null;
        fiftyTwoWeekLow = null;
    }

    public void refresh() {
        reset();
        getStock();
        labels.clear();
        if (stock != null) {
            int gap = 2;
            title = new ModLabel(x + WIDTH / 2, y + 5, Component.literal(StockMarket.name(stock)), ModColors.WHITE, ModLabel.Alignment.CENTER);
            labels.add(title);
            assetType = new ModLabel(x + 5, y + 20, Component.translatable(TranslationKeys.ASSET_TYPE).append(Component.literal(StockMarket.type(stock)).withColor(ModColors.GOLD.color)), ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(assetType);
            price = new ModLabel(x + 5, assetType.y + font.lineHeight + gap, Component.translatable(TranslationKeys.PRICE).append(Component.literal(ModHelper.format(StockMarket.price(stock))).withColor(ModColors.GOLD.color)), ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(price);

            if (stock.has(StockMarketKeys.MARKET_CAP)) {
                double marketCapValue = StockMarket.marketCap(stock);
                marketCap = new ModLabel(x + 5, price.y + font.lineHeight + gap,
                        Component.translatable(TranslationKeys.MARKET_CAP)
                                .append(Component.literal(ModHelper.format(marketCapValue)).withColor(ModColors.GOLD.color)),
                        ModColors.WHITE, ModLabel.Alignment.LEFT);
                labels.add(marketCap);
            }
            if (stock.has(StockMarketKeys.AUM)) {
                double aumValue = StockMarket.aum(stock);
                marketCap = new ModLabel(x + 5, price.y + font.lineHeight + gap,
                        Component.translatable(TranslationKeys.AUM)
                                .append(Component.literal(ModHelper.format(aumValue)).withColor(ModColors.GOLD.color)),
                        ModColors.WHITE, ModLabel.Alignment.LEFT);
                labels.add(marketCap);
            }

            int ongoingY = marketCap != null ? marketCap.y : price.y;
            double changeValue = StockMarket.change(stock);
            change = new ModLabel(x + 5, ongoingY + font.lineHeight + gap,
                    Component.translatable(TranslationKeys.CHANGE)
                            .append(Component.literal(ModHelper.format(changeValue)).withColor(ModHelper.getColor(changeValue).color)),
                    ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(change);

            double changePercentageValue = StockMarket.changePercentage(stock);
            changePercentage = new ModLabel(x + 5, change.y + font.lineHeight + gap,
                    Component.translatable(TranslationKeys.CHANGE_PERCENTAGE)
                            .append(Component.literal(ModHelper.format(changePercentageValue)).append("%").withColor(ModHelper.getColor(changePercentageValue).color)),
                    ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(changePercentage);

            dividendYield = new ModLabel(x + WIDTH / 2, y + 20,
                    Component.translatable(TranslationKeys.DIVIDEND_YIELD)
                            .append(Component.literal(ModHelper.format(StockMarket.dividendYield(stock))).append("%").withColor(ModColors.GOLD.color)),
                    ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(dividendYield);

            if (stock.has(StockMarketKeys.PE_RATIO)) {
                peRatio = new ModLabel(x + WIDTH / 2, dividendYield.y + font.lineHeight + gap,
                        Component.translatable(TranslationKeys.PE_RATIO)
                                .append(Component.literal(ModHelper.format(StockMarket.peRatio(stock))).withColor(ModColors.GOLD.color)),
                        ModColors.WHITE, ModLabel.Alignment.LEFT);
                labels.add(peRatio);
            }

            ongoingY = peRatio != null ? peRatio.y : dividendYield.y;
            fiftyTwoWeekHigh = new ModLabel(x + WIDTH / 2, ongoingY + font.lineHeight + gap,
                    Component.translatable(TranslationKeys.FIFTY_TWO_WEEK_HIGH)
                            .append(Component.literal(ModHelper.format(StockMarket.fiftyTwoWeekHigh(stock))).withColor(ModColors.GOLD.color)),
                    ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(fiftyTwoWeekHigh);

            fiftyTwoWeekLow = new ModLabel(x + WIDTH / 2, fiftyTwoWeekHigh.y + font.lineHeight + gap,
                    Component.translatable(TranslationKeys.FIFTY_TWO_WEEK_LOW)
                            .append(Component.literal(ModHelper.format(StockMarket.fiftyTwoWeekLow(stock))).withColor(ModColors.GOLD.color)),
                    ModColors.WHITE, ModLabel.Alignment.LEFT);
            labels.add(fiftyTwoWeekLow);
        }

        pastReturns.refresh();
    }

    private void getStock() {
        ModEntry selectedEntry = ModHelper.tradingScreen().getSelectedEntry();
        stock = selectedEntry != null ? StockMarket.get(selectedEntry.getTicker()) : null;
    }
}

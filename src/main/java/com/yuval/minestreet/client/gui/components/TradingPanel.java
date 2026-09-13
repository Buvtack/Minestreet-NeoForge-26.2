package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.network.chat.Component;

public class TradingPanel extends ModComponent {

    public static final int WIDTH = 228;
    public static final int HEIGHT = 168;

    private ModLabel title;
    private ModButton buy;
    private ModButton sell;
    private ModButton selectedButton;

    private JsonObject stock;

    public TradingPanel(int x, int y) {
        super(x, y);
        title = new ModLabel(x + WIDTH / 2, y + 5, Component.translatable(TranslationKeys.TRADING_PANEL_TITLE), 0xFFFFFFFF, ModLabel.Alignment.CENTER);

        buy = ModButton.builder(x + 5, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_BUY), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = buy;})
                .color(0x5510FF10)
                .panel(this)
                .build();

        sell = ModButton.builder(x + 50, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_SELL), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = sell;})
                .color(0x55FF1010)
                .panel(this)
                .build();
    }

    @Override
    public void tick() {
        buy.tick();
        sell.tick();
    }

    @Override
    public void doRender() {
        if (stock == null)
            return;

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, ModColors.STOCK_LIST_COLOR);
        title.render(graphics, mouseX, mouseY, partialTick);
        buy.render(graphics, mouseX, mouseY, partialTick);
        sell.render(graphics, mouseX, mouseY, partialTick);
    }

    public void setStock(JsonObject stock) {
        this.stock = stock;
        title.setContent(Component.literal(stock.get(StockMarketKeys.NAME).getAsString()));
        refresh();
    }

    private void refresh() {
        selectedButton = null;
    }

    public JsonObject getStock() {
        return stock;
    }

    public ModButton getSelectedButton() {
        return selectedButton;
    }
}

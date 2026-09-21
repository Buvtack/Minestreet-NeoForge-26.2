package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.mixin.client.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

public class TradingPanel extends ModComponent {

    public static final int WIDTH = 228;
    public static final int HEIGHT = 168;

    private ModLabel title;
    private ModLabel selectedItemLabel;
    private ModLabel selectedItemCountLabel;

    private ModButton buy;
    private ModButton sell;
    private ModButton selectedButton;

    private TextBox orderInput;
    private ModButton byQuantity;
    private ModButton byPercentage;
    private ModButton selectedInput;

    private ModButton send;

    private JsonObject stock;

    public TradingPanel(int x, int y) {
        super(x, y);
        title = new ModLabel(x + WIDTH / 2, y + 5, Component.translatable(TranslationKeys.TRADING_PANEL_TITLE), 0xFFFFFFFF, ModLabel.Alignment.CENTER);
        selectedItemLabel = new ModLabel(x + 5, y + HEIGHT - 40, Component.translatable(TranslationKeys.TRADING_PANEL_SELECTED_ITEM), 0xFFFFFFFF, ModLabel.Alignment.LEFT);
        selectedItemCountLabel = new ModLabel(selectedItemLabel.x + 5, selectedItemLabel.y, Component.empty(), 0xFFFFFFFF, ModLabel.Alignment.LEFT);

        buy = ModButton.builder(x + WIDTH / 2 - 45, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_BUY), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = buy;})
                .color(0x5510FF10)
                .panel(this)
                .build();

        sell = ModButton.builder(x + WIDTH / 2 + 5, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_SELL), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = sell;})
                .color(0x55FF1010)
                .panel(this)
                .build();

        orderInput = new TextBox(Minecraft.getInstance().font, x + 5, y + 50, 100, 20, Component.literal("Amount:"));
        orderInput.init();
        ((ScreenAccessor) Minecraft.getInstance().gui.screen()).callAddRenderableWidget(orderInput);

        byQuantity = ModButton.builder(orderInput.getX() + orderInput.getWidth(), orderInput.getY(), 20, Component.literal("ABS"), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedInput = byQuantity)
                .color(0x55005EF5)
                .panel(this)
                .tooltip(Component.literal("Select this to enter the amount\n you want to buy/sell of the\n selected item"))
                .build();

        byPercentage = ModButton.builder(byQuantity.x + byQuantity.width, byQuantity.y, 20, Component.literal("%"), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedInput = byPercentage)
                .color(0x55005EF5)
                .panel(this)
                .tooltip(Component.literal("Select this to enter the percentage you want\n to buy/sell of the selected item. \n(e.g. if you have 100 diamonds and you type \n50, you will buy/sell 50 diamonds)"))
                .build();

        Component sendText = Component.literal("Send Order");
        int sendWidth = font.width(sendText.getString()) + 10;
        send = ModButton.builder(x + WIDTH / 2 - sendWidth / 2, y + HEIGHT - 25, sendWidth, sendText, ModLabel.Alignment.CENTER)
                .onClick(() -> {
                    TradingStationScreen screen = (TradingStationScreen) ModHelper.screen();
                    screen.send();
                })
                .color(0x77005EF5)
                .panel(this)
                .build();
    }

    @Override
    public void tick() {
        buy.tick();
        sell.tick();
        byQuantity.tick();
        byPercentage.tick();
        orderInput.tick();
        send.tick();
        tickSelectedStackLabel();
    }

    private void tickSelectedStackLabel() {
        TradingStationScreen screen = (TradingStationScreen) Minecraft.getInstance().gui.screen();
        if (screen.getSelectedStack() != null && !screen.getSelectedStack().isEmpty()) {
            selectedItemLabel.setContent(Component.translatable(TranslationKeys.TRADING_PANEL_SELECTED_ITEM).append(Component.literal(": ")).append(Component.literal(screen.getSelectedStack().getItemName().getString()).withColor(TextColor.GOLD)));
            selectedItemCountLabel.setContent(Component.literal("x").append(Component.literal(Integer.toString(ModHelper.getItemCount(screen.getSelectedStack())))));
            selectedItemCountLabel.x = selectedItemLabel.x + font.width(selectedItemLabel.content.getString()) + 25;
        }
    }

    @Override
    public void doRender() {
        if (stock == null)
            return;

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, ModColors.STOCK_LIST_COLOR);
        title.render(graphics, mouseX, mouseY, partialTick);

        if (ModHelper.selectedStack())
            selectedItemLabel.render(graphics, mouseX, mouseY, partialTick);

        buy.render(graphics, mouseX, mouseY, partialTick);
        sell.render(graphics, mouseX, mouseY, partialTick);
        byQuantity.render(graphics, mouseX, mouseY, partialTick);
        byPercentage.render(graphics, mouseX, mouseY, partialTick);
        orderInput.extractRenderState(graphics, mouseX, mouseY, partialTick);
        orderInput.render(graphics);
        send.render(graphics, mouseX, mouseY, partialTick);
        renderSelectedStackCount();
    }

    private void renderSelectedStackCount() {
        TradingStationScreen screen = (TradingStationScreen) ModHelper.screen();
        if (screen.getSelectedStack() != null && !screen.getSelectedStack().isEmpty()) {
            selectedItemCountLabel.render(graphics, mouseX, mouseY, partialTick);
            graphics.item(screen.getSelectedStack(), selectedItemCountLabel.x - 17, selectedItemCountLabel.y - 4);
        }
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

    public ModButton getSelectedInput() {
        return selectedInput;
    }

    public boolean isFocused() {
        return orderInput != null && orderInput.isFocused();
    }

    public void setFocused(boolean focused) {
        if (focused)
            Minecraft.getInstance().gui.screen().setFocused(orderInput);

        orderInput.setFocused(focused);
        orderInput.setCursorPosition(orderInput.getValue().length());
        orderInput.setHighlightPos(orderInput.getCursorPosition());
    }

    public boolean keyPressed(KeyEvent event) {
        return orderInput.keyPressed(event);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return orderInput.mouseClicked(event, doubleClick);
    }
}

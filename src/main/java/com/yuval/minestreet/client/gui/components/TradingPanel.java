package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.StockMarketClient;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.common.Order;
import com.yuval.minestreet.common.Position;
import com.yuval.minestreet.mixin.client.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class TradingPanel extends DimensionalModComponent {

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

    private ModButton closePosition;

    private ExtendedStockInfo stockInfo;
    private ExtendedPositionInfo positionInfo;

    private ModButton send;

    public TradingPanel(int x, int y) {
        super(x, y, WIDTH, HEIGHT, 3);
        title = new ModLabel(x + WIDTH / 2, y + 5, Component.translatable(TranslationKeys.TRADING_PANEL_TITLE), ModColors.WHITE, ModLabel.Alignment.CENTER);
        selectedItemLabel = new ModLabel(x + 5, y + HEIGHT - 38, Component.translatable(TranslationKeys.TRADING_PANEL_SELECTED_ITEM), ModColors.WHITE, ModLabel.Alignment.LEFT);
        selectedItemCountLabel = new ModLabel(selectedItemLabel.x + 5, selectedItemLabel.y, Component.empty(), ModColors.WHITE, ModLabel.Alignment.LEFT);

        buy = ModButton.builder(x + WIDTH / 2 - 45, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_BUY), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = buy;}).color(ModColors.BUY).panel(this).build();

        sell = ModButton.builder(x + WIDTH / 2 + 5, y + 20, 40, Component.translatable(TranslationKeys.TRADING_PANEL_SELL), ModLabel.Alignment.CENTER)
                .onClick(() -> {selectedButton = sell;}).color(ModColors.SELL).panel(this).build();

        orderInput = new TextBox(Minecraft.getInstance().font, x + 5, y + 48, 100, 20, Component.literal("Amount:"));
        orderInput.init();
        ((ScreenAccessor) Minecraft.getInstance().gui.screen()).callAddRenderableWidget(orderInput);

        byQuantity = ModButton.builder(orderInput.getX() + orderInput.getWidth(), orderInput.getY(), 20, Component.literal("ABS"), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedInput = byQuantity).color(ModColors.NEUTRAL).panel(this).build();
        byQuantity.tooltip(new ModTooltip(byQuantity.x, byQuantity.y, 30, ModColors.STOCK_LIST_COLOR, Component.literal("Select this to enter the exact amount of the selected item you'd like to buy/sell.")));

        byPercentage = ModButton.builder(byQuantity.x + byQuantity.width, byQuantity.y, 20, Component.literal("%"), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedInput = byPercentage).color(ModColors.NEUTRAL).panel(this).build();
        byPercentage.tooltip(new ModTooltip(byPercentage.x, byPercentage.y, 30, ModColors.STOCK_LIST_COLOR, Component.literal("Select this to enter the percentage you want to buy/sell of the selected item. (e.g. if you have 100 diamonds and you type 50, you will buy/sell 50 diamonds)")));

        closePosition = ModButton.builder(byPercentage.x + byPercentage.width, byPercentage.y, 82, Component.translatable(TranslationKeys.TRADING_PANEL_CLOSE_POSITION), ModLabel.Alignment.CENTER)
                .onClick(() -> ((TradingStationScreen) ModHelper.screen()).closePosition()).color(ModColors.SELL).panel(this).build();

        Component sendText = Component.literal("Send Order");
        int sendWidth = font.width(sendText.getString()) + 10;
        send = ModButton.builder(x + WIDTH / 2 - sendWidth / 2, y + HEIGHT - 25, sendWidth, sendText, ModLabel.Alignment.CENTER)
                .onClick(ModHelper.tradingScreen()::send).color(ModColors.NEUTRAL.alphaify(0.15F)).panel(this).build();

        stockInfo = new ExtendedStockInfo(x + 5, y + 73);
        positionInfo = new ExtendedPositionInfo(x + WIDTH / 2 - 5, y + 73);
    }

    @Override
    public void doTick() {
        buy.tick();
        sell.tick();
        byQuantity.tick();
        byPercentage.tick();

        if (ModHelper.tradingScreen().getSelectedPosition() != null)
            closePosition.tick();

        orderInput.tick();
        send.tick();
        tickSelectedStackLabel();
        stockInfo.tick();
        positionInfo.tick();
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
        if (ModHelper.tradingScreen().getSelectedEntry() == null)
            return;

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, ModColors.STOCK_LIST_COLOR.color);
        title.render(graphics, mouseX, mouseY, partialTick);

        if (ModHelper.selectedStack())
            selectedItemLabel.render(graphics, mouseX, mouseY, partialTick);

        buy.render(graphics, mouseX, mouseY, partialTick);
        sell.render(graphics, mouseX, mouseY, partialTick);
        orderInput.extractRenderState(graphics, mouseX, mouseY, partialTick);
        orderInput.render(graphics);
        send.render(graphics, mouseX, mouseY, partialTick);
        renderSelectedStackCount();
        stockInfo.render(graphics, mouseX, mouseY, partialTick);
        positionInfo.render(graphics, mouseX, mouseY, partialTick);
        byQuantity.render(graphics, mouseX, mouseY, partialTick);
        byPercentage.render(graphics, mouseX, mouseY, partialTick);
        if (ModHelper.tradingScreen().getSelectedPosition() != null)
            closePosition.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSelectedStackCount() {
        TradingStationScreen screen = (TradingStationScreen) ModHelper.screen();
        if (screen.getSelectedStack() != null && !screen.getSelectedStack().isEmpty()) {
            selectedItemCountLabel.render(graphics, mouseX, mouseY, partialTick);
            graphics.item(screen.getSelectedStack(), selectedItemCountLabel.x - 17, selectedItemCountLabel.y - 4);
        }
    }

    public void refresh() {
        PositionEntry positionEntry = ModHelper.tradingScreen().getSelectedPosition();
        JsonObject stock = null;
        if (positionEntry != null) {
            Position position = ModHelper.tradingScreen().getSelectedPosition().getPosition();
            positionInfo.setPosition(position);
            stock = StockMarket.get(position.getTicker());
        } else if (ModHelper.tradingScreen().getSelectedStock() != null) {
            positionInfo.setPosition(null);
            stock = ModHelper.tradingScreen().getSelectedStock().stock;
        }

        if (stock != null) {
            title.setContent(Component.literal(StockMarket.name(stock)));
            stockInfo.setStock(stock);
            reset();
        }
    }

    private void reset() {
        selectedButton = null;
    }

    public ModButton getSelectedButton() {
        return selectedButton;
    }

    public ModButton getSelectedInput() {
        return selectedInput;
    }

    public double getAmount() {
        try {
            if (selectedInput == byQuantity)
                return Double.parseDouble(orderInput.getValue());
            else {
                double fraction = Double.parseDouble(orderInput.getValue()) / 100;
                TradingStationScreen screen = (TradingStationScreen) ModHelper.screen();
                if (selectedButton == buy)
                    return ModHelper.getItemCount(screen.getSelectedStack()) * fraction;
                else {
                    Position position = ModHelper.tradingScreen().getSelectedPosition().getPosition();
                    if (position == null)
                        return 0;

                    return position.worth() * fraction;
                }
            }
        } catch (Exception e) {
            return 0.0D;
        }
    }

    public Order.Type orderType() {
        return selectedButton == buy ? Order.Type.BUY : Order.Type.SELL;
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

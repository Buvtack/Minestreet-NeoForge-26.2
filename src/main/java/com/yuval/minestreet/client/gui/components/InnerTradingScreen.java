package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class InnerTradingScreen extends DimensionalModComponent {

    public static final int WIDTH = 228;
    public static final int HEIGHT = 168;

    private ModLabel emptyMessage;

    public TradingPanel tradingPanel;
    public DividendPanel dividendPanel;
    public ChartPanel chartPanel;
    public FundamentalsInfoPanel infoPanel;

    private ModButton tradingPanelTab;
    private ModButton dividendPanelTab;
    private ModButton chartPanelTab;
    private ModButton infoPanelTab;
    private ModButton selectedTab;

    private Map<ModButton, ModComponent> buttonToPanelMap;

    public InnerTradingScreen(int x, int y) {
        super(x, y, WIDTH, HEIGHT, 3);

        emptyMessage = new ModLabel(x + WIDTH / 2, y + HEIGHT / 2, Component.translatable(TranslationKeys.NO_ASSET_SELECTED), ModColors.WHITE, ModLabel.Alignment.CENTER);

        int screenHeight = ModHelper.screen().height;
        tradingPanelTab = ModButton.builder(5, screenHeight - 25, 20, Component.empty(), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedTab = tradingPanelTab).color(ModColors.TRANSPARENT);
        tradingPanelTab.tooltip(new ModTooltip(tradingPanelTab.x, tradingPanelTab.y, 50, ModColors.STOCK_LIST_COLOR, Component.literal("Trading"))).build();
        tradingPanelTab.icon(new ModIcon(tradingPanelTab.x, tradingPanelTab.y, tradingPanelTab.width, tradingPanelTab.height, Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/icons/trading_tab_icon.png")));
        tradingPanelTab.addRenderTask("highlight", () -> highlight(tradingPanelTab));

        dividendPanelTab = ModButton.builder(tradingPanelTab.x + tradingPanelTab.width, tradingPanelTab.y, 20, Component.empty(), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedTab = dividendPanelTab).color(ModColors.TRANSPARENT);
        dividendPanelTab.tooltip(new ModTooltip(dividendPanelTab.x, dividendPanelTab.y, 50, ModColors.STOCK_LIST_COLOR, Component.literal("Dividends"))).build();
        dividendPanelTab.icon(new ModIcon(dividendPanelTab.x, dividendPanelTab.y, dividendPanelTab.width, dividendPanelTab.height, Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/icons/dividend_tab_icon.png")));
        dividendPanelTab.addRenderTask("highlight", () -> highlight(dividendPanelTab));

        chartPanelTab = ModButton.builder(dividendPanelTab.x + dividendPanelTab.width, dividendPanelTab.y, 20, Component.empty(), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedTab = chartPanelTab).color(ModColors.TRANSPARENT);
        chartPanelTab.tooltip(new ModTooltip(chartPanelTab.x, chartPanelTab.y, 50, ModColors.STOCK_LIST_COLOR, Component.literal("Chart"))).build();
        chartPanelTab.icon(new ModIcon(chartPanelTab.x, chartPanelTab.y, chartPanelTab.width, chartPanelTab.height, Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/icons/chart_tab_icon.png")));
        chartPanelTab.addRenderTask("highlight", () -> highlight(chartPanelTab));

        infoPanelTab = ModButton.builder(chartPanelTab.x + chartPanelTab.width, chartPanelTab.y, 20, Component.empty(), ModLabel.Alignment.CENTER)
                .onClick(() -> selectedTab = infoPanelTab).color(ModColors.TRANSPARENT);
        infoPanelTab.tooltip(new ModTooltip(infoPanelTab.x, infoPanelTab.y, 50, ModColors.STOCK_LIST_COLOR, Component.literal("Fundamentals (info)")));
        infoPanelTab.icon(new ModIcon(infoPanelTab.x, infoPanelTab.y, infoPanelTab.width, infoPanelTab.height, Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/icons/info.png")));
        infoPanelTab.addRenderTask("highlight", () -> highlight(infoPanelTab));

        selectedTab = tradingPanelTab;

        tradingPanel = new TradingPanel(x, y);
        dividendPanel = new DividendPanel(x, y);
        chartPanel = new ChartPanel(x, y);
        infoPanel = new FundamentalsInfoPanel(x, y);

        buttonToPanelMap = new HashMap<>();
        buttonToPanelMap.put(tradingPanelTab, tradingPanel);
        buttonToPanelMap.put(dividendPanelTab, dividendPanel);
        buttonToPanelMap.put(chartPanelTab, chartPanel);
        buttonToPanelMap.put(infoPanelTab, infoPanel);
    }

    private void highlight(ModButton tab) {
        if (selectedTab == tab)
            graphics.fill(tab.x, tab.y, tab.x + tab.width, tab.y + tab.height, ModColors.TRANSPARENT.alphaify(0.35F).color);
    }

    @Override
    public void doTick() {
        tradingPanelTab.tick();
        dividendPanelTab.tick();
        chartPanelTab.tick();
        infoPanelTab.tick();

        if (selectedTab == null)
            return;

        emptyMessage.tick();
        panel().tick();
    }

    @Override
    public void doRender() {
        if (selectedTab != null)
            panel().render(graphics, mouseX, mouseY, partialTick);

        if (ModHelper.tradingScreen().getSelectedEntry() == null && selectedTab != dividendPanelTab)
            emptyMessage.render(graphics, mouseX, mouseY, partialTick);

        tradingPanelTab.render(graphics, mouseX, mouseY, partialTick);
        dividendPanelTab.render(graphics, mouseX, mouseY, partialTick);
        chartPanelTab.render(graphics, mouseX, mouseY, partialTick);
        infoPanelTab.render(graphics, mouseX, mouseY, partialTick);
    }

    public void refresh() {
        tradingPanel.refresh();
        infoPanel.refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return panel().keyPressed(event);
    }

    @Override
    public boolean isFocused() {
        return panel().isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        panel().setFocused(focused);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return panel().mouseClicked(event, doubleClick);
    }

    private ModComponent panel() {
        return buttonToPanelMap.get(selectedTab);
    }
}

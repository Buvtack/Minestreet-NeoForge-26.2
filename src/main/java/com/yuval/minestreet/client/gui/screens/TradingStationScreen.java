package com.yuval.minestreet.client.gui.screens;

import com.google.gson.JsonObject;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.components.StockEntry;
import com.yuval.minestreet.client.gui.components.TextBox;
import com.yuval.minestreet.client.gui.components.TradingPanel;
import com.yuval.minestreet.network.packets.RequestStockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TradingStationScreen extends ModScreen<TradingStationMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/container/trading_station.png");

    private Inventory inventory;

    private ModSlot[][] inventorySlots;
    private ModSlot[] hotbarSlots;
    private ModSlot selectedSlot;
    private ItemStack selectedStack;
    private StockEntry selectedStock;

    private TextBox searchStock;
    private boolean shouldSendRequest = false;
    private String lastSearched;

    private TradingPanel panel;

    private List<StockEntry> stocks;

    public TradingStationScreen(TradingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventory = inventory;
        searchStock = new TextBox(getFont(),5, 5, 100, 20, Component.translatable("minestreet.gui.searchStock"));
        inventorySlots = new ModSlot[3][9];
        hotbarSlots = new ModSlot[9];
        stocks = new LinkedList<>();
    }

    @Override
    public void init() {
        super.init();
        panel = new TradingPanel(width / 2 - TradingPanel.WIDTH / 2 + 1, height / 2 - TradingPanel.HEIGHT / 2 - 39);
        searchStock.init();
        addRenderableWidget(searchStock);

        initInventory();
        initStocks();
    }

    private void initInventory() {
        final int startX = width / 2 - 79;
        final int startY = height / 2 + 52;

        int x = startX;
        int y = startY;

        int slotWidth = 18, slotHeight = 18;

        for (int row = 0; row < 3; row++) {
            x = startX;
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9 + 9;
                ItemStack stack = inventory.getItem(slot);
                inventorySlots[row][col] = new ModSlot(slot, x, y, slotWidth, slotHeight, stack);
                x += slotWidth;
            }
            y += slotHeight;
        }

        y += 4;
        x = startX;
        for (int col = 0; col < 9; col++) {
            ItemStack stack = inventory.getItem(col);
            hotbarSlots[col] = new ModSlot(col, x, y, slotWidth, slotHeight, stack);
            x += slotWidth;
        }
    }

    private void initStocks() {
        stocks.clear();
        int y = 30;
        int x = 5;
        for (String ticker : StockMarket.INITIAL_TICKERS) {
            JsonObject stock = StockMarket.get(ticker);
            stocks.add(new StockEntry(x, y, stock));
            y += 2 + StockEntry.HEIGHT;
        }
    }

    @Override
    public void containerTick() {
        for (int i = 0; i < stocks.size(); i++) {
            StockEntry entry = stocks.get(i);
            entry.tick();
        }

        if (selectedStock != null && panel.getStock() != selectedStock.stock)
            panel.setStock(selectedStock.stock);

        panel.tick();
        searchStock.tick();
        String searched = searchStock.getValue();
        if (searchStock.finishedTyping && !searched.equals(lastSearched)) {
            if (!searched.isBlank())
                ClientPacketDistributor.sendToServer(new RequestStockPacket(searched));
            else
                updateStockEntryList();
            //updateStockEntryList();
            lastSearched = searched;
        }
    }

    public void updateStockEntryList() {
        String searched = searchStock.getValue().toUpperCase();
        stocks.clear();
        int i = 0;
        int limit = 15;
        int y = 30;
        int x = 5;
        int gap = 2;

        Object tickerList = searched.isBlank() ? Arrays.asList(StockMarket.INITIAL_TICKERS) : StockMarket.storedStocks.keySet();
        for (String ticker : (Iterable<String>) tickerList) {
            if (ticker.contains(searched)) {
                if (i > limit)
                    break;

                JsonObject stock = StockMarket.get(ticker);
                stocks.add(new StockEntry(x, y, stock));
                y += gap + StockEntry.HEIGHT;
                i++;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.graphics = graphics;

        int x = width / 2 - 192;
        int y = height / 2 - 144;
        graphics.blit(TEXTURE, x, y, x + 384, y + 288, 0, 1, 0, 1);
        renderSlots(graphics, mouseX, mouseY);

        searchStock.render(graphics);
        searchStock.extractRenderState(graphics, mouseX, mouseY, partialTick);

        panel.render(graphics, mouseX, mouseY, partialTick);

        renderStocks(mouseX, mouseY, partialTick);
    }

    private void renderSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int row = 0; row < inventorySlots.length; row++)
            for (int col = 0; col < inventorySlots[0].length; col++) {
                ModSlot slot = inventorySlots[row][col];
                slot.render(graphics, mouseX, mouseY);
            }

        for (int col = 0; col < 9; col++) {
            ModSlot slot = hotbarSlots[col];
            slot.render(graphics, mouseX, mouseY);
        }
    }

    private void renderStocks(int mouseX, int mouseY, float partialTicks) {
        for (StockEntry entry : stocks)
            entry.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput) {}
    @Override
    protected boolean isHovering(int left, int top, int w, int h, double xm, double ym) { return false; }

    @Override
    public boolean keyPressed(KeyEvent event) {

        if (searchStock != null && searchStock.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchStock.setFocused(false);
                return true;
            }

            return searchStock.keyPressed(event);
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        shouldSendRequest = false;
        if (searchStock != null && searchStock.isFocused())
            return searchStock.charTyped(event);

        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (searchStock.mouseClicked(event, doubleClick)) {
            searchStock.setFocused(true);
            return searchStock.mouseClicked(event, doubleClick);
        }

        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        for (int i = 0; i < inventorySlots.length; i++) {
            for (int j = 0; j < inventorySlots[0].length; j++) {
                ModSlot slot = inventorySlots[i][j];
                if (slot.hovered(mouseX, mouseY) && !slot.stack.isEmpty()) {
                    selectedSlot = slot;
                    selectedStack = slot.stack;
                    StockMarket.searchAndStore(searchStock.getValue());
                }
            }
        }

        for (int i = 0; i < hotbarSlots.length; i++) {
            ModSlot slot = hotbarSlots[i];
            if (slot.hovered(mouseX, mouseY) && !slot.stack.isEmpty()) {
                selectedSlot = slot;
                selectedStack = slot.stack;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if (searchStock != null && this.searchStock.isMouseOver(mouseX, mouseY))
            return false;

        return super.hasClickedOutside(mouseX, mouseY, left, top);
    }

    public void setSelectedStock(StockEntry stock) {
        selectedStock = stock;
    }

    public StockEntry getSelectedStock() {
        return selectedStock;
    }

    private class ModSlot {

        private int x;
        private int y;
        private int width;
        private int height;
        private ItemStack stack;
        private int index;
        private int row;
        private int col;

        private static final int SELECTED_COLOR = 0x8808BB81;

        public ModSlot(int index, int x, int y, int width, int height, ItemStack stack) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.stack = stack;
        }

        public boolean hovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            if (this == selectedSlot || selectedStack != null && stack.is(selectedStack.getItem()))
                graphics.fill(x, y, x + width - 1, y + height - 1, SELECTED_COLOR);

            graphics.item(stack, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
            if (hovered(mouseX, mouseY)) {
                graphics.fill(x, y, x + width - 1, y + height - 1, 0x55FFFFFF);

                if (!stack.is(Items.AIR))
                    graphics.setTooltipForNextFrame(
                            font,
                            getTooltipFromItem(minecraft, stack),
                            stack.getTooltipImage(),
                            mouseX, mouseY
                    );
            }
        }
    }
}

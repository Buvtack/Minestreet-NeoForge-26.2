package com.yuval.minestreet.client.gui.screens;

import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.components.TextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class TradingStationScreen extends AbstractContainerScreen<TradingStationMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "textures/gui/container/trading_station.png");

    private Inventory inventory;

    private ModSlot[][] inventorySlots;
    private ModSlot[] hotbarSlots;

    private TextBox searchStock;

    public TradingStationScreen(TradingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventory = inventory;
        searchStock = new TextBox(getFont(),5, 5, 100, 20, Component.translatable("minestreet.gui.searchStock"));
        inventorySlots = new ModSlot[3][9];
        hotbarSlots = new ModSlot[9];
    }

    @Override
    public void init() {
        super.init();
        searchStock.init();
        addRenderableWidget(searchStock);

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
                inventorySlots[row][col] = new ModSlot(x, y, slotWidth, slotHeight, stack);
                x += slotWidth;
            }
            y += slotHeight;
        }

        y += 4;
        x = startX;
        for (int col = 0; col < 9; col++) {
            ItemStack stack = inventory.getItem(col);
            hotbarSlots[col] = new ModSlot(x, y, slotWidth, slotHeight, stack);
            x += slotWidth;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = width / 2 - 192;
        int y = height / 2 - 144;
        graphics.blit(TEXTURE, x, y, x + 384, y + 288, 0, 1, 0, 1);
        renderSlots(graphics, mouseX, mouseY);

        searchStock.render(graphics);
        searchStock.extractRenderState(graphics, mouseX, mouseY, partialTick);
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (searchStock.mouseClicked(event, doubleClick)) {
            searchStock.setFocused(true);
            return searchStock.mouseClicked(event, doubleClick);
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if (searchStock != null && this.searchStock.isMouseOver(mouseX, mouseY))
            return false;

        return super.hasClickedOutside(mouseX, mouseY, left, top);
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

        public ModSlot(int x, int y, int width, int height, ItemStack stack) {
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

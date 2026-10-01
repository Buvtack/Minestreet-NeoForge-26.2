package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.CommonModHelper;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.gui.ModColor;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import com.yuval.minestreet.common.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;

public class PositionEntry extends ModEntry {

    private Section ticker;
    private Section worth;
    private Section pnl;
    private Section pnlPercentage;
    private ItemStack item;

    private Position position;

    public PositionEntry(int x, int y, Position position) {
        super(x, y);

        this.position = position;
        int startX = x + 8;
        ticker = new Section(position.getTicker(), startX, y, WIDTH / 4 - 1, HEIGHT);
        worth = new Section(position.worth(), startX + WIDTH / 4 - 1, y, WIDTH / 4 - 1, HEIGHT);
        pnl = new Section(position.pnl(), startX + WIDTH / 2 - 2, y, WIDTH / 4 - 1, HEIGHT);
        pnlPercentage = new Section(position.pnlPercentage(), "%", startX + 3 * WIDTH / 4 - 3, y, WIDTH / 4, HEIGHT);
        item = new ItemStack(BuiltInRegistries.ITEM.getValue(position.getItem()));
    }

    @Override
    public void tick() {
        if (CommonModHelper.isNotLeftClicking())
            clickable = true;

        if (CommonModHelper.isLeftClicking() && clickable) {
            if (ModHelper.screen() instanceof TradingStationScreen screen && isMouseOver(mouseX, mouseY)) {
                screen.setSelectedPosition(this);
            }
        }
    }

    @Override
    public void doRender() {
        graphics.fill(x - 4, y, x + WIDTH, y + HEIGHT, getBackgroundColor());
        ticker.render(0xFFFFFFFF);
        worth.render(0xFFFFFFFF);
        pnl.render(pnl.getColor());
        pnlPercentage.render(pnl.getColor());

        graphics.pose().pushMatrix();
        graphics.pose().scale(0.75F);
        graphics.item(item, (int) ((x - 4) / 0.75F), (int) ((y - 1) / 0.75F));
        graphics.pose().popMatrix();
    }

    private int getBackgroundColor() {
        ModColor color = ModColors.STOCK_LIST_COLOR.alphaify(fade() / 5F);
        if (ModHelper.screen() instanceof TradingStationScreen screen)
            return screen.getSelectedPosition() == this ? color.alphaify(0.5F).color : color.color;

        return color.color;
    }

    @Override
    public void refresh() {
        worth.refresh(position.worth());
        pnl.refresh(position.pnl());
        pnlPercentage.refresh(position.pnlPercentage(), "%");
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String getTicker() {
        return position.getTicker();
    }
}

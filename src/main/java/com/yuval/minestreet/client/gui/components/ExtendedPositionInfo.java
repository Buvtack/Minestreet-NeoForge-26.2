package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.TranslationKeys;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.common.Position;
import net.minecraft.network.chat.Component;

public class ExtendedPositionInfo extends ModComponent {

    private Position position;

    private ModLabel pnl;
    private ModLabel pnlPercentage;
    private ModLabel entryPrice;
    private ModLabel amount;

    public ExtendedPositionInfo(int x, int y) {
        super(x, y);
    }

    public void setPosition(Position position) {
        this.position = position;
        int gap = 2;
        pnl = new ModLabel(x, y, Component.translatable(TranslationKeys.PNL), ModColors.WHITE, ModLabel.Alignment.LEFT);
        pnlPercentage = new ModLabel(x, pnl.y + font.lineHeight + gap, Component.translatable(TranslationKeys.PNL_PERCENTAGE), ModColors.WHITE, ModLabel.Alignment.LEFT);
        entryPrice = new ModLabel(x, pnlPercentage.y + font.lineHeight + gap, Component.translatable(TranslationKeys.ENTRY_PRICE), ModColors.WHITE, ModLabel.Alignment.LEFT);
        amount = new ModLabel(x, entryPrice.y + font.lineHeight + gap, Component.translatable(TranslationKeys.AMOUNT), ModColors.WHITE, ModLabel.Alignment.LEFT);
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void tick() {
        if (position == null)
            return;

        pnl.tick();
        pnlPercentage.tick();
        entryPrice.tick();
        amount.tick();

        String ticker = position.getTicker();
        double pnlValue = position.pnl();
        double pnlPercentageValue = position.pnlPercentage();
        double entryPriceValue = position.getPrice();
        double amountValue = position.getAmount();
        Component pnlContent = Component.translatable(TranslationKeys.PNL).append(ModHelper.format(pnlValue)).withColor(ModHelper.getColor(pnlValue).color);
        Component pnlPercentageContent = Component.translatable(TranslationKeys.PNL_PERCENTAGE).append(ModHelper.format(pnlPercentageValue)).append("%").withColor(ModHelper.getColor(pnlPercentageValue).color);
        Component entryPriceContent = Component.translatable(TranslationKeys.ENTRY_PRICE).append(ModHelper.format(entryPriceValue));
        Component amountContent = Component.translatable(TranslationKeys.AMOUNT).append(ModHelper.format(amountValue));
        pnl.setContent(pnlContent);
        pnlPercentage.setContent(pnlPercentageContent);
        entryPrice.setContent(entryPriceContent);
        amount.setContent(amountContent);
    }

    @Override
    public void doRender() {
        if (position == null)
            return;

        pnl.render(graphics, mouseX, mouseY, partialTick);
        pnlPercentage.render(graphics, mouseX, mouseY, partialTick);
        entryPrice.render(graphics, mouseX, mouseY, partialTick);
        amount.render(graphics, mouseX, mouseY, partialTick);
    }
}

package com.yuval.minestreet.client.gui.components;

import net.minecraft.resources.Identifier;

public class ModIcon extends DimensionalModComponent {

    private Identifier iconId;

    public ModIcon(int x, int y, int width, int height, Identifier iconId) {
        super(x, y, width, height, 3);
        this.iconId = iconId;
    }

    @Override
    public void doTick() {

    }

    @Override
    public void doRender() {
        graphics.blit(iconId, x + 2, y + 2, x + width - 2, y + height - 2, 0, 1, 0, 1);
    }
}

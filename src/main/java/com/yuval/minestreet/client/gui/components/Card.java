package com.yuval.minestreet.client.gui.components;

import com.yuval.minestreet.client.gui.ModColor;
import net.minecraft.network.chat.Component;

public class Card extends DimensionalModComponent {

    private ModColor background;

    private ModLabel title;
    private ModLabel body;

    private Card(int x, int y, int width, int height) {
        super(x, y, width, height, 3);
    }

    public static CardBuilder builder() {
        return new CardBuilder();
    }

    @Override
    public void doTick() {
        title.tick();
        body.tick();
    }

    @Override
    public void doRender() {
        graphics.fill(x, y, x + width, y + height, background.color);
        title.render(graphics, mouseX, mouseY, partialTick);
        body.render(graphics, mouseX, mouseY, partialTick);
    }

    public static class CardBuilder {

        private final Card card;

        public CardBuilder() {
            card = new Card(0, 0, 0, 0);
        }

        public CardBuilder dimensions(int x, int y, int width, int height) {
            card.x = x;
            card.y = y;
            card.width = width;
            card.height = height;
            return this;
        }

        public CardBuilder background(ModColor color) {
            card.background = color;
            return this;
        }

        public CardBuilder title(Component content, ModLabel.Alignment alignment, ModColor color) {
            int margin = 1;
            int x = alignment == ModLabel.Alignment.CENTER ? (card.x + margin) + (card.width - margin) / 2 :
                    (alignment == ModLabel.Alignment.RIGHT) ? card.x + card.width - margin : card.x + margin;
            card.title = new ModLabel(x, card.y + 2, content, color, alignment);
            return this;
        }

        public CardBuilder body(Component content, ModLabel.Alignment alignment, ModColor color) {
            int margin = 1;
            int x = alignment == ModLabel.Alignment.CENTER ? (card.x + margin) + (card.width - margin) / 2 :
                    (alignment == ModLabel.Alignment.RIGHT) ? card.x + card.width - margin : card.x + margin;
            card.body = new ModLabel(x, card.y + card.height / 2 + 1, content, color, alignment);
            return this;
        }

        public Card build() {
            return card;
        }
    }
}

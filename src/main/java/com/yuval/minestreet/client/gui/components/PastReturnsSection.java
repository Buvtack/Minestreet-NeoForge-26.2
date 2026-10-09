package com.yuval.minestreet.client.gui.components;

import com.google.gson.JsonObject;
import com.yuval.minestreet.PastPerformance;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.client.ModHelper;
import com.yuval.minestreet.client.gui.ModColor;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PastReturnsSection extends DimensionalModComponent {

    private Card day;
    private Card week;
    private Card month;
    private Card half;
    private Card ytd;
    private Card year;
    private Card fiveYears;
    private Card tenYears;
    private Card allTime;

    private JsonObject stock;
    private List<Card> cards;

    public PastReturnsSection(int x, int y, int width, int height) {
        super(x, y, width, height, 3);

        cards = new ArrayList<>();
        init();
    }

    private void init() {
        cards.clear();
        ModEntry entry = ModHelper.tradingScreen().getSelectedEntry();
        if (entry == null)
            return;

        stock = StockMarket.get(entry.getTicker());
        if (stock == null)
            return;

        final int cardWidth = 56;
        final int cardHeight = 20;

        PastPerformance perf = StockMarket.performance(stock);

        Double dayPerf = perf.day();
        day = Card.builder().dimensions(x + 5, y + 5, cardWidth, cardHeight)
                .background(getColor(dayPerf))
                .title(Component.literal("1 Day"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(dayPerf), ModLabel.Alignment.CENTER, ModColors.WHITE).build();
        cards.add(day);

        Double weekPerf = perf.week();
        week = Card.builder().dimensions(x + width / 2 - cardWidth / 2, y + 5, cardWidth, cardHeight)
                .background(getColor(weekPerf))
                .title(Component.literal("1 Week"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(weekPerf), ModLabel.Alignment.CENTER, ModColors.WHITE).build();
        cards.add(week);

        Double monthPerf = perf.month();
        month = Card.builder().dimensions(x + width - cardWidth - 5, y + 5, cardWidth, cardHeight)
                .background(getColor(monthPerf))
                .title(Component.literal("1 Month"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(monthPerf), ModLabel.Alignment.CENTER, ModColors.WHITE).build();
        cards.add(month);

        Double halfPerf = perf.half();
        half = Card.builder().dimensions(x + 5, y + height / 2 - cardHeight / 2, cardWidth, cardHeight)
                .background(getColor(halfPerf))
                .title(Component.literal("6 Months"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(halfPerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(half);

        Double ytdPerf = perf.ytd();
        ytd = Card.builder().dimensions(x + width / 2 - cardWidth / 2, y + height / 2 - cardHeight / 2, cardWidth, cardHeight)
                .background(getColor(ytdPerf))
                .title(Component.literal("YTD"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(ytdPerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(ytd);

        Double yearPerf = perf.year();
        year = Card.builder().dimensions(x + width - cardWidth - 5, y + height / 2 - cardHeight / 2, cardWidth, cardHeight)
                .background(getColor(yearPerf))
                .title(Component.literal("1 Year"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(yearPerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(year);

        Double fiveYearsPerf = perf.fiveYears();
        fiveYears = Card.builder().dimensions(x + 5, y + height - cardHeight - 5, cardWidth, cardHeight)
                .background(getColor(fiveYearsPerf))
                .title(Component.literal("5 Years"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(fiveYearsPerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(fiveYears);

        Double tenYearsPerf = perf.tenYears();
        tenYears = Card.builder().dimensions(x + width / 2 - cardWidth / 2, y + height - cardHeight - 5, cardWidth, cardHeight)
                .background(getColor(tenYearsPerf))
                .title(Component.literal("10 Years"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(tenYearsPerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(tenYears);

        Double allTimePerf = perf.allTime();
        allTime = Card.builder().dimensions(x + width - cardWidth - 5, y + height - cardHeight - 5, cardWidth, cardHeight)
                .background(getColor(allTimePerf))
                .title(Component.literal("All Time"), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .body(getBody(allTimePerf), ModLabel.Alignment.CENTER, ModColors.WHITE)
                .build();
        cards.add(allTime);
    }

    public void refresh() {
        init();
    }

    private Component getBody(Double d) {
        return d != null ? Component.literal(ModHelper.format(d)).append("%").withColor(ModHelper.getColor(d).color) : Component.literal("-");
    }

    private ModColor getColor(Double d) {
        return d != null ? ModHelper.getColor(d).transparensify(0.75F) : ModColors.TRANSPARENT_GRAY;
    }

    @Override
    public void doTick() {
        if (stock == null)
            return;

        for (Card card : cards)
            card.tick();
    }

    @Override
    public void doRender() {
        if (stock == null)
            return;

        for (Card card : cards)
            card.render(graphics, mouseX, mouseY, partialTick);
    }
}

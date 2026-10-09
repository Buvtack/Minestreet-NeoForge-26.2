package com.yuval.minestreet.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.ModColor;
import com.yuval.minestreet.client.gui.ModColors;
import com.yuval.minestreet.client.gui.screens.TradingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ModHelper {

    private static final NumberFormat compactFormatter = NumberFormat.getCompactNumberInstance(
            Locale.US,
            NumberFormat.Style.SHORT
    );

    private static final NumberFormat simpleFormatter = new DecimalFormat("#,##0.00");

    static {
        compactFormatter.setMaximumFractionDigits(2);
        compactFormatter.setMinimumFractionDigits(0);
    }

    public static Screen screen() {
        return Minecraft.getInstance().gui.screen();
    }
    public static TradingStationScreen tradingScreen() {
        return (TradingStationScreen) screen();
    }

    public static Player player() {
        return Minecraft.getInstance().player;
    }

    public static boolean isTradingStation() {
        return screen() instanceof TradingStationScreen;
    }

    public static boolean selectedStack() {
        TradingStationScreen screen = (TradingStationScreen) screen();
        return screen.getSelectedStack() != null && !screen.getSelectedStack().isEmpty();
    }

    public static int getItemCount(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        Inventory inventory = player.getInventory();
        int result = 0;
        for (ItemStack inventoryStack : inventory) {
            if (stack.is(inventoryStack.getItem()))
                result += inventoryStack.count();
        }
        return result;
    }

    public static List<String> split(String text, int width) {
        List<String> words = Arrays.asList(text.split(" "));
        List<String> result = new LinkedList<>();
        String line = "";
        for (String word : words) {
            int wordLength = word.length();
            int lineLength = line.length();
            if (lineLength + wordLength <= width)
                line += word + " ";
            else {
                line = line.substring(0, line.length() - 1);
                result.add(new String(line));
                line = word + " ";
            }
        }
        if (!line.isBlank())
            result.add(line);

        return result;
    }

    public static String format(double d) {
        if (d >= 10_000)
            return compactFormatter.format(d);

        return simpleFormatter.format(d);
    }

    public static ModColor getColor(Number num) {
        return num.doubleValue() >= 0 ? (num.doubleValue() > 0 ? ModColors.PROFIT : ModColors.WHITE) : ModColors.LOSS;
    }

    public static void roundImage(NativeImage image) {
        int radius = image.getWidth() > image.getHeight() ? image.getWidth() / 2 : image.getHeight() / 2;
        int rx = radius;
        int ry = radius;
        int rWidth = image.getWidth() - radius * 2;
        int rHeight = image.getHeight() - radius * 2;
        double softness = 4;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                double distance = distanceFromRect(x, y, rx, ry, rWidth, rHeight) + 0.5;

                double gap = distance - radius + 3;
                float alpha = (float) Math.max(0.0, Math.min(1.0, gap / softness));

                image.setPixelABGR(x, y, new ModColor(image.getPixel(x, y)).transparensify(alpha).toABGR());
            }
        }
    }

    private static double distanceFromRect(int x, int y, int rx, int ry, int rWidth, int rHeight) {
        int distX;
        if (x < rx)
            distX = rx - x;
        else if (x <= rx + rWidth)
            distX = 0;
        else
            distX = x - rx + rWidth;

        int distY;
        if (y < ry)
            distY = ry - y;
        else if (y <= ry + rHeight)
            distY = 0;
        else
            distY = y - ry + rHeight;

        return Math.sqrt(distX * distX + distY * distY);
    }
}

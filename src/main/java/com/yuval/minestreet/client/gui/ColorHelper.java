package com.yuval.minestreet.client.gui;

public class ColorHelper {

    public static int lerpColor(float ratio, int color1, int color2) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int alpha = (int) net.minecraft.util.Mth.lerp(ratio, a1, a2);
        int red   = (int) net.minecraft.util.Mth.lerp(ratio, r1, r2);
        int green = (int) net.minecraft.util.Mth.lerp(ratio, g1, g2);
        int blue  = (int) net.minecraft.util.Mth.lerp(ratio, b1, b2);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

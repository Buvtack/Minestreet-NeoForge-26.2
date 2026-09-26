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

    public static int alphaify(float ratio, int color) {
        int alpha = alpha(color);
        int remainder = 0xFF - alpha;
        int newAlpha = alpha + (int)(ratio * remainder);
        return build(newAlpha, red(color), green(color), blue(color));
    }

    public static int transparensify(float ratio, int color) {
        int newAlpha = (int)(ratio * alpha(color));
        return build(newAlpha, red(color), green(color), blue(color));
    }

    public static int redify(float ratio, int color) {
        int red = red(color);
        int remainder = 0xFF - red;
        int newRed = red + (int)(remainder * ratio);
        return build(alpha(color), newRed, green(color), blue(color));
    }

    public static int deredify(float ratio, int color) {
        int newRed = (int)(red(color) * ratio);
        return build(alpha(color), newRed, green(color), blue(color));
    }

    public static int greenify(float ratio, int color) {
        int green = green(color);
        int remainder = 0xFF - green;
        int newGreen = green + (int)(remainder * ratio);
        return build(alpha(color), red(color), newGreen, blue(color));
    }

    public static int degreenify(float ratio, int color) {
        int newGreen = (int)(green(color) * ratio);
        return build(alpha(color), red(color), newGreen, blue(color));
    }

    public static int blueify(float ratio, int color) {
        int blue = blue(color);
        int remainder = 0xFF - blue;
        int newBlue = blue + (int)(remainder * ratio);
        return build(alpha(color), red(color), green(color), newBlue);
    }

    public static int deblueify(float ratio, int color) {
        int newBlue = (int)(blue(color) * ratio);
        return build(alpha(color), red(color), green(color), newBlue);
    }

    public static int brighten(float ratio, int color) {
        int red = red(color);
        int blue = blue(color);
        int green = green(color);

        int brighter = (int) (ratio * 0xFF);
        red = Math.min(0xFF, red + brighter);
        blue = Math.min(0xFF, blue + brighter);
        green = Math.min(0xFF, green + brighter);
        return build(alpha(color), red, green, blue);
    }

    public static int darken(float ratio, int color) {
        int red = red(color);
        int blue = blue(color);
        int green = green(color);

        int darker = (int)(ratio * 0xFF);
        red = Math.max(0, red - darker);
        green = Math.max(0, green - darker);
        blue = Math.max(0, blue - darker);
        return build(alpha(color), red, green, blue);
    }

    public static int saturate(float ratio, int color) {
        int result = color;
        result = redify(ratio, color);
        result = greenify(ratio, color);
        result = blueify(ratio, color);
        return result;
    }

    public static int desaturate(float ratio, int color) {
        int result = color;
        result = deredify(ratio, color);
        result = degreenify(ratio, color);
        result = deblueify(ratio, color);
        return result;
    }

    public static int alpha(int color) {
        return color >> 24;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int build(int alpha, int red, int green, int blue) {
        alpha <<= 24;
        red <<= 16;
        green <<= 8;
        return alpha + red + green + blue;
    }
}

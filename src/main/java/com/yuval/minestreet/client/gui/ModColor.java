package com.yuval.minestreet.client.gui;

public class ModColor {

    public int color;

    public ModColor(int color) {
        this.color = color;
    }

    public ModColor alphaify(float ratio) {
        int alpha = alpha(color);
        int remainder = 0xFF - alpha;
        int newAlpha = alpha + (int) (ratio * remainder);
        return new ModColor(build(newAlpha, red(color), green(color), blue(color)));
    }

    public ModColor transparensify(float ratio) {
        int newAlpha = (int) (ratio * alpha(color));
        return new ModColor(build(newAlpha, red(color), green(color), blue(color)));
    }

    public ModColor redify(float ratio) {
        int red = red(color);
        int remainder = 0xFF - red;
        int newRed = red + (int) (remainder * ratio);
        return new ModColor(build(alpha(color), newRed, green(color), blue(color)));
    }

    public ModColor deredify(float ratio) {
        int newRed = (int) (red(color) * (1 - ratio));
        return new ModColor(build(alpha(color), newRed, green(color), blue(color)));
    }

    public ModColor greenify(float ratio) {
        int green = green(color);
        int remainder = 0xFF - green;
        int newGreen = green + (int) (remainder * ratio);
        return new ModColor(build(alpha(color), red(color), newGreen, blue(color)));
    }

    public ModColor degreenify(float ratio) {
        int newGreen = (int) (green(color) * (1 - ratio));
        return new ModColor(build(alpha(color), red(color), newGreen, blue(color)));
    }

    public ModColor blueify(float ratio) {
        int blue = blue(color);
        int remainder = 0xFF - blue;
        int newBlue = blue + (int) (remainder * ratio);
        return new ModColor(build(alpha(color), red(color), green(color), newBlue));
    }

    public ModColor deblueify(float ratio) {
        int newBlue = (int) (blue(color) * (1 - ratio));
        return new ModColor(build(alpha(color), red(color), green(color), newBlue));
    }

    public ModColor brighten(float ratio) {
        int red = red(color);
        int blue = blue(color);
        int green = green(color);

        int brighter = (int) (ratio * 0xFF);
        red = Math.min(0xFF, red + brighter);
        blue = Math.min(0xFF, blue + brighter);
        green = Math.min(0xFF, green + brighter);
        return new ModColor(build(alpha(color), red, green, blue));
    }

    public ModColor darken(float ratio) {
        int red = red(color);
        int blue = blue(color);
        int green = green(color);

        int darker = (int) (ratio * 0xFF);
        red = Math.max(0, red - darker);
        green = Math.max(0, green - darker);
        blue = Math.max(0, blue - darker);
        return new ModColor(build(alpha(color), red, green, blue));
    }

    public ModColor saturate(float ratio) {
        int avg = avg(color);
        ModColor result = new ModColor(color);
        if (red(color) > avg)
            result = result.redify(ratio);
        else if (red(color) < avg)
            result = result.deredify(ratio);

        if (green(color) > avg)
            result = result.greenify(ratio);
        else if (green(color) < avg)
            result = result.degreenify(ratio);

        if (blue(color) > avg)
            result = result.blueify(ratio);
        else if (blue(color) < avg)
            result = result.deblueify(ratio);

        return result;
    }

    public ModColor desaturate(float ratio) {
        int avg = avg(color);
        ModColor result = new ModColor(color);
        if (red(color) > avg)
            result = result.deredify(ratio);
        else if (red(color) < avg)
            result = result.redify(ratio);

        if (green(color) > avg)
            result = result.degreenify(ratio);
        else if (green(color) < avg)
            result = result.greenify(ratio);

        if (blue(color) > avg)
            result = result.deblueify(ratio);
        else if (blue(color) < avg)
            result = result.blueify(ratio);

        return result;
    }

    public int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public int blue(int color) {
        return color & 0xFF;
    }

    public int build(int alpha, int red, int green, int blue) {
        alpha <<= 24;
        red <<= 16;
        green <<= 8;
        return alpha + red + green + blue;
    }

    private int avg(int color) {
        return (red(color) + green(color) + blue(color)) / 3;
    }
}

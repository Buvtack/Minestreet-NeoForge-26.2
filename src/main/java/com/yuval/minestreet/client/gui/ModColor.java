package com.yuval.minestreet.client.gui;

public class ModColor {

    public int color;

    public ModColor(int color) {
        this.color = color;
    }

    public ModColor alphaify(float ratio) {
        int alpha = alpha();
        int remainder = 0xFF - alpha;
        int newAlpha = alpha + (int) (ratio * remainder);
        return new ModColor(build(newAlpha, red(), green(), blue()));
    }

    public ModColor transparensify(float ratio) {
        int newAlpha = (int) ((1 - ratio) * alpha());
        return new ModColor(build(newAlpha, red(), green(), blue()));
    }

    public ModColor redify(float ratio) {
        int red = red();
        int remainder = 0xFF - red;
        int newRed = red + (int) (remainder * ratio);
        return new ModColor(build(alpha(), newRed, green(), blue()));
    }

    public ModColor deredify(float ratio) {
        int newRed = (int) (red() * (1 - ratio));
        return new ModColor(build(alpha(), newRed, green(), blue()));
    }

    public ModColor greenify(float ratio) {
        int green = green();
        int remainder = 0xFF - green;
        int newGreen = green + (int) (remainder * ratio);
        return new ModColor(build(alpha(), red(), newGreen, blue()));
    }

    public ModColor degreenify(float ratio) {
        int newGreen = (int) (green() * (1 - ratio));
        return new ModColor(build(alpha(), red(), newGreen, blue()));
    }

    public ModColor blueify(float ratio) {
        int blue = blue();
        int remainder = 0xFF - blue;
        int newBlue = blue + (int) (remainder * ratio);
        return new ModColor(build(alpha(), red(), green(), newBlue));
    }

    public ModColor deblueify(float ratio) {
        int newBlue = (int) (blue() * (1 - ratio));
        return new ModColor(build(alpha(), red(), green(), newBlue));
    }

    public ModColor brighten(float ratio) {
        int red = red();
        int blue = blue();
        int green = green();

        int brighter = (int) (ratio * 0xFF);
        red = Math.min(0xFF, red + brighter);
        blue = Math.min(0xFF, blue + brighter);
        green = Math.min(0xFF, green + brighter);
        return new ModColor(build(alpha(), red, green, blue));
    }

    public ModColor darken(float ratio) {
        int red = red();
        int blue = blue();
        int green = green();

        int darker = (int) (ratio * 0xFF);
        red = Math.max(0, red - darker);
        green = Math.max(0, green - darker);
        blue = Math.max(0, blue - darker);
        return new ModColor(build(alpha(), red, green, blue));
    }

    public ModColor saturate(float ratio) {
        int avg = avg();
        ModColor result = new ModColor(color);
        if (red() > avg)
            result = result.redify(ratio);
        else if (red() < avg)
            result = result.deredify(ratio);

        if (green() > avg)
            result = result.greenify(ratio);
        else if (green() < avg)
            result = result.degreenify(ratio);

        if (blue() > avg)
            result = result.blueify(ratio);
        else if (blue() < avg)
            result = result.deblueify(ratio);

        return result;
    }

    public ModColor desaturate(float ratio) {
        int avg = avg();
        ModColor result = new ModColor(color);
        if (red() > avg)
            result = result.deredify(ratio);
        else if (red() < avg)
            result = result.redify(ratio);

        if (green() > avg)
            result = result.degreenify(ratio);
        else if (green() < avg)
            result = result.greenify(ratio);

        if (blue() > avg)
            result = result.deblueify(ratio);
        else if (blue() < avg)
            result = result.blueify(ratio);

        return result;
    }

    public int alpha() {
        return (color >> 24) & 0xFF;
    }

    public int red() {
        return (color >> 16) & 0xFF;
    }

    public int green() {
        return (color >> 8) & 0xFF;
    }

    public int blue() {
        return color & 0xFF;
    }

    public int build(int alpha, int red, int green, int blue) {
        alpha <<= 24;
        red <<= 16;
        green <<= 8;
        return alpha | red | green | blue;
    }

    private int avg() {
        return (red() + green() + blue()) / 3;
    }

    public int toABGR() {
        return build(alpha(), blue(), green(), red());
    }
}

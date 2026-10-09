package com.yuval.minestreet.client;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.yuval.minestreet.StockMarket;
import com.yuval.minestreet.StockMarketKeys;
import com.yuval.minestreet.WolfOfMinestreet;
import com.yuval.minestreet.client.gui.ModColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Base64;

public class Logos {

    public static void addLogo(JsonObject stock) {
        String ticker = StockMarket.ticker(stock);
        if (!stock.has(StockMarketKeys.LOGO)) {
            NativeImage image = new NativeImage(100, 100, true);
            image.fillRect(0, 0, image.getWidth(), image.getHeight(), ModColors.TRANSPARENT.color);
            registerImage(ticker, image);
        }

        byte[] pngBytes = Base64.getDecoder().decode(stock.get(StockMarketKeys.LOGO).getAsString());
        try {
            NativeImage image = NativeImage.read(pngBytes);
            registerImage(ticker, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerImage(String ticker, NativeImage image) {
        ModHelper.roundImage(image);
        String imagePath = "logo/" + ticker.toLowerCase();
        DynamicTexture texture = new DynamicTexture(() -> imagePath, image);
        Identifier textureId = Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, imagePath);
        Minecraft.getInstance().getTextureManager().register(textureId, texture);
    }

    public static Identifier logo(String ticker) {
        return Identifier.fromNamespaceAndPath(WolfOfMinestreet.MODID, "logo/" + ticker.toLowerCase());
    }
}

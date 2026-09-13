package com.yuval.minestreet;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CommonModHelper {

    public static boolean isNotLeftClicking() {
        long windowHandle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE;
    }

    public static boolean isLeftClicking() {
        long windowHandle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    @Deprecated
    public static float grow() {
        float guiScale = Minecraft.getInstance().options.guiScale().get();
        int sub = (int) (guiScale / 2);
        return guiScale / Math.max(guiScale - sub, 1);
    }

    @Deprecated
    public static float shrink() {
        float guiScale = Minecraft.getInstance().options.guiScale().get();
        int sub = (int) (guiScale / 2);
        return Math.max(guiScale - sub, 1) / guiScale;
    }
}

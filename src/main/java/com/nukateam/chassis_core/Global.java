package com.nukateam.chassis_core;

import com.nukateam.chassis_core.common.util.helpers.timer.TickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class Global {
    public static TickTimer CLIENT_TIMER = new TickTimer();

    public static Double mouseX = null;
    public static Double mouseY = null;

    public static Entity referenceMob = null;

    public static void saveMousePos() {
        var window = Minecraft.getInstance().getWindow().getWindow();
        var x = new double[1];
        var y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        mouseX = x[0];
        mouseY = y[0];
    }
}

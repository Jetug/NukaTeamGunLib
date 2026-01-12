package com.nukateam.ntgl.modules.wheel;


import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    private static final int ACTION_WHEEL_KEY = GLFW.GLFW_KEY_R;
    private static boolean keyPressed = false;

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        if (event.getKey() == ACTION_WHEEL_KEY) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                keyPressed = true;
                ActionWheelManager.getInstance().onKeyPressed();
            } else if (event.getAction() == GLFW.GLFW_RELEASE && keyPressed) {
                ActionWheelManager.getInstance().onKeyReleased();
                keyPressed = false;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseMove(InputEvent.MouseScrollingEvent event) {
        // Можно использовать скролл для навигации
        if (ActionWheelManager.getInstance().isWheelActive()) {
            // Логика скролла
            event.setCanceled(true);
        }
    }
}
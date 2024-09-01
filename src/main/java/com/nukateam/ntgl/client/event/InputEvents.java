package com.nukateam.ntgl.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {
    public static float X = 0;
    public static float Y = 0;
    public static float Z = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onKeyInput(InputEvent.Key event) {
        if(event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT){
            switch (event.getKey()) {
                case GLFW.GLFW_KEY_KP_1 -> X += 0.1f;
                case GLFW.GLFW_KEY_KP_2 -> Y += 0.1f;
                case GLFW.GLFW_KEY_KP_3 -> Z += 0.1f;
                case GLFW.GLFW_KEY_KP_4 -> X -= 0.1f;
                case GLFW.GLFW_KEY_KP_5 -> Y -= 0.1f;
                case GLFW.GLFW_KEY_KP_6 -> Z -= 0.1f;
                case GLFW.GLFW_KEY_KP_ENTER -> {
                    X = 0;
                    Y = 0;
                    Z = 0;
                }
            }
        }
    }
}

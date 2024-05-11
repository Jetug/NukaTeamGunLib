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
        if(event.getAction() == GLFW.GLFW_PRESS){
            switch (event.getKey()) {
                case GLFW.GLFW_KEY_X -> X += 0.01f;
                case GLFW.GLFW_KEY_Y -> Y += 0.01f;
                case GLFW.GLFW_KEY_Z -> Z += 0.01f;
                case GLFW.GLFW_KEY_C -> X -= 0.01f;
                case GLFW.GLFW_KEY_U -> Y -= 0.01f;
                case GLFW.GLFW_KEY_V -> Z -= 0.01f;
            }
        }
    }
}
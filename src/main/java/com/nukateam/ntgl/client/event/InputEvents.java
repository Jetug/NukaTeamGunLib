package com.nukateam.ntgl.client.event;

import com.mojang.text2speech.Narrator;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.handler.ClientReloadHandler;
import com.nukateam.ntgl.common.foundation.item.MagazineItem;
import com.nukateam.ntgl.common.network.HandAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageHandAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.nukateam.ntgl.client.input.KeyBinds.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {
    public static int X = 0;
    public static int Y = 0;
    public static int Z = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onKeyInput(InputEvent.@NotNull Key event) {
        var minecraft = Minecraft.getInstance();
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        if(event.getAction() == GLFW.GLFW_RELEASE){
            if(event.getKey() == KEY_FIRE_SELECT.getKey().getValue()){
                PacketHandler.getPlayChannel().sendToServer(new S2CMessageHandAction(hand, HandAction.SWITCH_FIRE_MODE));
            }
            else if(event.getKey() == KEY_AMMO_SELECT.getKey().getValue()){
                PacketHandler.getPlayChannel().sendToServer(new S2CMessageHandAction(hand, HandAction.SWITCH_AMMO));
            }
        }

        if(event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT){
            if(Ntgl.isDebugging()) {
                switch (event.getKey()) {
                    case GLFW.GLFW_KEY_KP_1 -> X += 1;
                    case GLFW.GLFW_KEY_KP_2 -> Y += 1;
                    case GLFW.GLFW_KEY_KP_3 -> Z += 1;
                    case GLFW.GLFW_KEY_KP_4 -> X -= 1;
                    case GLFW.GLFW_KEY_KP_5 -> Y -= 1;
                    case GLFW.GLFW_KEY_KP_6 -> Z -= 1;
                    case GLFW.GLFW_KEY_KP_ENTER -> {
                        X = 0;
                        Y = 0;
                        Z = 0;
                    }
                }
            }
        }
    }
}

package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.handler.ClientActions;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
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
import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.addClientEntity;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {
    public static int X = 0;
    public static int Y = 0;
    public static int Z = 0;
    public static boolean isHidden = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onKeyInput(InputEvent.@NotNull Key event) {
        var minecraft = Minecraft.getInstance();
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var player = minecraft.player;

        if(minecraft.player != null && minecraft.level != null && minecraft.isRunning() && !minecraft.isPaused()) {
            if (event.getAction() == GLFW.GLFW_RELEASE) {
                if (event.getKey() == KEY_FIRE_SELECT.getKey().getValue()) {
                    ClientActions.switchFireMode(hand);
                }
                else if (event.getKey() == KEY_AMMO_SELECT.getKey().getValue()) {
                    ClientActions.switchAmmo(hand, player);
                }
            }
        }

        handleDebugKeys(event);
    }

    private static void handleDebugKeys(InputEvent.@NotNull Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT) {
            if (Ntgl.isDebugging()) {
                switch (event.getKey()) {
                    case GLFW.GLFW_KEY_KP_1 -> X += 1;
                    case GLFW.GLFW_KEY_KP_2 -> Y += 1;
                    case GLFW.GLFW_KEY_KP_3 -> Z += 1;
                    case GLFW.GLFW_KEY_KP_4 -> X -= 1;
                    case GLFW.GLFW_KEY_KP_5 -> Y -= 1;
                    case GLFW.GLFW_KEY_KP_6 -> Z -= 1;
                    case GLFW.GLFW_KEY_KP_MULTIPLY -> isHidden = !isHidden;
                    case GLFW.GLFW_KEY_KP_ENTER -> {
                        var level = Minecraft.getInstance().level;
                        var entity = new FlyingGib(ModEntityTypes.FLYING_GIBS.get(), level);

                        entity.setPos(Minecraft.getInstance().player.position());
                        addClientEntity(entity);

                        X = 0;
                        Y = 0;
                        Z = 0;
                    }
                }
            }
        }
    }
}

package com.nukateam.ntgl.client.handlers;

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
                int key = event.getKey();
                if (key == KEY_DEBUG_X_ADD.getKey().getValue()) {
                    X += 1;
                } else if (key == KEY_DEBUG_Y_ADD.getKey().getValue()) {
                    Y += 1;
                } else if (key == KEY_DEBUG_Z_ADD.getKey().getValue()) {
                    Z += 1;
                } else if (key == KEY_DEBUG_X_SUB.getKey().getValue()) {
                    X -= 1;
                } else if (key == KEY_DEBUG_Y_SUB.getKey().getValue()) {
                    Y -= 1;
                } else if (key == KEY_DEBUG_Z_SUB.getKey().getValue()) {
                    Z -= 1;
                } else if (key == KEY_DEBUG_SHOW.getKey().getValue()) {
                    isHidden = !isHidden;
                } else if (key == KEY_DEBUG_ZERO.getKey().getValue()) {
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

package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.handler.ClientActions;
import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.nukateam.ntgl.client.input.KeyBinds.*;
import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.addClientEntity;
import static com.nukateam.ntgl.client.util.handler.ShootingHandler.isInGame;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {

    @SubscribeEvent
    public static void onKeyPressed(TickEvent.ClientTickEvent event) {
        handleKeys();
        handleDebugKeys();
    }

    private static void handleKeys() {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        if (player == null || !isInGame())
            return;

        if (KeyBinds.KEY_ATTACHMENTS.consumeClick()) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
        }
        if (KeyBinds.KEY_RELOAD.consumeClick()) {
            ClientReloadHandler.get().startReloading();
        }
        if (KeyBinds.KEY_UNLOAD.consumeClick()) {
            ClientReloadHandler.get().unloadAmmo(InteractionHand.MAIN_HAND);
            ClientReloadHandler.get().unloadAmmo(InteractionHand.OFF_HAND);
        }
        if (KeyBinds.KEY_INSPECT.consumeClick()){
            ClientActions.inspectWeapon(player);
        }
        if(KeyBinds.KEY_FIRE_SELECT.consumeClick()){
            ClientActions.switchFireMode(hand);
        }
        if(KeyBinds.KEY_AMMO_SELECT.consumeClick()){
            ClientActions.switchAmmo(hand, player);
        }
    }

    private static void handleDebugKeys() {
        if (Ntgl.isDebugging()) {
            if (KEY_DEBUG_X_ADD.isDown()) {
                ClientDebug.X += 1;
            } else if (KEY_DEBUG_Y_ADD.isDown()) {
                ClientDebug.Y += 1;
            } else if (KEY_DEBUG_Z_ADD.isDown()) {
                ClientDebug.Z += 1;
            } else if (KEY_DEBUG_X_SUB.isDown()) {
                ClientDebug.X -= 1;
            } else if (KEY_DEBUG_Y_SUB.isDown()) {
                ClientDebug.Y -= 1;
            } else if (KEY_DEBUG_Z_SUB.isDown()) {
                ClientDebug.Z -= 1;
            } else if (KEY_DEBUG_SHOW.isDown()) {
                ClientDebug.isHidden = !ClientDebug.isHidden;
            } else if (KEY_DEBUG_ZERO.isDown()) {
                var level = Minecraft.getInstance().level;
                var entity = new FlyingGib(ModEntityTypes.FLYING_GIBS.get(), level);

                entity.setPos(Minecraft.getInstance().player.position());
                addClientEntity(entity);

                ClientDebug.X = 0;
                ClientDebug.Y = 0;
                ClientDebug.Z = 0;
            }
        }
    }
}

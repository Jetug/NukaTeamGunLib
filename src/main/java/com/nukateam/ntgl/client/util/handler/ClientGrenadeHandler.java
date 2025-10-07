package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.network.KeyAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.util.util.GunModifierHelper.canRenderInOffhand;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientGrenadeHandler {
    private static final Map<InteractionHand, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event){
        var minecraft = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.END){
            var hand = InteractionHand.MAIN_HAND;

            if(minecraft.options.keyAttack.isDown()){
                var stack = minecraft.player.getItemInHand(hand);
                if(stack.getItem() instanceof IThrowable && !TRACKER_MAP.containsKey(hand)){
                    TRACKER_MAP.put(hand, new Tracker(minecraft.player, hand));
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.HOLD, hand));
                }
            }
            else {
                if(TRACKER_MAP.containsKey(hand)) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.RELEASE, hand));
                    TRACKER_MAP.remove(hand);
                }
            }

//            TRACKER_MAP.forEach((k, v) -> v.tick());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled()) return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null) return;

        if (event.isAttack()) {
            var heldItem = player.getMainHandItem();

            if (heldItem.getItem() instanceof IThrowable) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        } else if (event.isUseItem()) {
            var offhandItem = player.getOffhandItem();

            if (offhandItem.getItem() instanceof IThrowable && canRenderInOffhand(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    private static class Tracker {
        private final InteractionHand arm;
        private final ItemStack stack;
        private final IThrowable throwable;
        private final int maxPrepare;
        private final int maxThrow;
        private final int maxLife;
        private final LivingEntity entity;

        private int prepareTick = 0;
        private int throwTick = 0;
        private int lifeTick = 0;

        private boolean isPreparing = true;
        private boolean isThrowing = false;

        private Tracker(LivingEntity entity, InteractionHand arm) {
            this.arm = arm;
            this.entity = entity;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = throwable.getConfig().getThrowable().getPrepareTime();
            this.maxThrow = throwable.getConfig().getThrowable().getThrowTime();
            this.maxLife = throwable.getConfig().getThrowable().getProjectile().getLife();

            prepareTick = maxPrepare;
            throwTick = maxThrow;
            lifeTick = maxLife;
        }

        private void explode() {
            stop();
        }

        private void stop() {
            TRACKER_MAP.remove(arm);
        }

        private boolean isSameWeapon() {
            return !this.stack.isEmpty() && entity.getItemInHand(arm) == this.stack;
        }
    }
}

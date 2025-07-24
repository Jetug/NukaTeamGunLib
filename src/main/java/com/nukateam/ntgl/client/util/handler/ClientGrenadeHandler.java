package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.base.utils.trackers.GrenadeTracker;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientGrenadeHandler {
    private static final Map<InteractionHand, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event){
        var minecraft = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.START){
            if(minecraft.options.keyAttack.isDown()){
                var mainHand = minecraft.player.getMainHandItem();
                var offHand = minecraft.player.getOffhandItem();
                if(mainHand.getItem() instanceof IThrowable){

                }
                else
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

        private int throwTick = 0;
        private int timeLeft = 0;
        private int lifeTick = 0;

        private boolean isPreparing = false;
        private boolean isThrowing = false;

        private Tracker(LivingEntity entity, InteractionHand arm) {
            this.arm = arm;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = throwable.getConfig().getGeneral().getPrepareTime();
            this.maxThrow = throwable.getConfig().getGeneral().getThrowTime();
            this.maxLife = throwable.getConfig().getProjectile().getLife();
        }

//        public void tick(){
//            if()
//        }

        private boolean isSameWeapon(LivingEntity entity) {
            return !this.stack.isEmpty() && entity.getItemInHand(arm) == this.stack;
        }
    }
}

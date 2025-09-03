package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.ntgl.common.util.trackers.ShootTracker.*;
import static com.nukateam.ntgl.common.network.ServerPlayHandler.*;
import static com.nukateam.ntgl.common.util.util.GunModifierHelper.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ServerEvent {
    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase != TickEvent.Phase.START) {
            handleAutoReload((ServerPlayer) event.player, InteractionHand.MAIN_HAND);
            handleAutoReload((ServerPlayer) event.player, InteractionHand.OFF_HAND);
        }
    }

    private static void handleAutoReload(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var shootTracker = getShootTracker(player, hand);
        var isReloading = ModSyncedDataKeys.getReloadKey(hand).getValue(player);

        if (!player.isCreative()
                && isGun(stack)
                && !isReloading
                && isAutoReloading(new GunData(stack, player))
                && shootTracker.cooldownEnded()
                && !GunStateHelper.hasAmmo(stack)
        ) {
            reloadGun(hand, player);
        }
    }
}

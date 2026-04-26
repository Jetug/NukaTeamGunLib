package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.nukateam.ntgl.common.util.trackers.ShootTracker.*;
import static com.nukateam.ntgl.common.network.ServerPlayHandler.*;
import static com.nukateam.ntgl.common.util.util.WeaponModifierHelper.*;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ServerEvent {
    @SubscribeEvent
    public static void onServerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            handleAutoReload((ServerPlayer) event.getEntity(), InteractionHand.MAIN_HAND);
            handleAutoReload((ServerPlayer) event.getEntity(), InteractionHand.OFF_HAND);
        }
    }

    private static void handleAutoReload(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var shootTracker = getShootTracker(player, hand);
        var isReloading = ModSyncedDataKeys.getReloadKey(hand).getValue(player);
        var data = new WeaponData(stack, player);

        if (!player.isCreative()
                && isGun(stack)
                && !isReloading
                && isAutoReloading(data)
                && shootTracker.cooldownEnded()
                && !WeaponStateHelper.hasAmmo(data)
        ) {
            reloadGun(hand, data);
        }
    }
}

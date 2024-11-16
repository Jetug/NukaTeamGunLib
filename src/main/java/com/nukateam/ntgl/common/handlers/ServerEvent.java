package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.ntgl.common.base.utils.ShootTracker.getShootTracker;
import static com.nukateam.ntgl.common.network.ServerPlayHandler.reloadGun;

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
        var isGun = GunModifierHelper.isGun(stack);

        if (!player.isCreative() && isGun && isAutoReloading(stack) && shootTracker.hasCooldown()) {
            reloadGun(hand, player);
        }
    }

    private static boolean isAutoReloading(ItemStack stack) {
        return GunModifierHelper.getGun(stack).getGeneral().isAutoReloading();
    }
}

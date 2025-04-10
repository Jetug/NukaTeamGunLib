package com.nukateam.ntgl.common.handlers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.utils.EquipTracker;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    private static final Map<Pair<HumanoidArm, Player>, ItemStack> lastSelectedSlots = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.START || event.side == LogicalSide.CLIENT) return;

        var player = event.player;
        var mainHandKey = new Pair<>(HumanoidArm.RIGHT, player);
        var offHandKey  = new Pair<>(HumanoidArm.LEFT , player);

        extracted(mainHandKey, player.getMainHandItem());
        extracted(offHandKey , player.getOffhandItem ());
    }

    private static void extracted(Pair<HumanoidArm, Player> key, ItemStack newItem) {
        var lastSlot = lastSelectedSlots.getOrDefault(key, ItemStack.EMPTY);
        var arm = key.getFirst();
        var player = key.getSecond();

        if (newItem != lastSlot) {
            if (newItem.getItem() instanceof GunItem) {
                EquipTracker.startEquip(player, arm);
//                if (!player.getCooldowns().isOnCooldown(newItem.getItem())) {
//                    player.getCooldowns().addCooldown(newItem.getItem(), 20);
//                }
            }

            lastSelectedSlots.put(key, newItem);
        }
    }
}
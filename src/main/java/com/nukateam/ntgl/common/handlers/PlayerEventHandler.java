package com.nukateam.ntgl.common.handlers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.utils.trackers.EquipTracker;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    public static final UUID SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-5e6f-7890-1234-567890abcdef");
    public static final String MOVEMENT_SPEED = "custom_movement_speed";

    private static final Map<Pair<InteractionHand, Player>, Pair<ItemStack, Integer> > lastSelectedSlots = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick2(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            var player = event.player;
            var heldItem = player.getMainHandItem();
            var movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);

            if (movementSpeed != null) {
                movementSpeed.removeModifier(SPEED_MODIFIER_ID);

                if (heldItem.getItem() instanceof WeaponItem) {
                    movementSpeed.removeModifier(SPEED_MODIFIER_ID);
                    movementSpeed.addTransientModifier(new AttributeModifier(
                            SPEED_MODIFIER_ID,
                            MOVEMENT_SPEED,
                            GunModifierHelper.getModifiedMovementSpeed(new GunData(heldItem, player)),
                            AttributeModifier.Operation.MULTIPLY_BASE
                    ));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            tryEquip(event.player, InteractionHand.MAIN_HAND);
            tryEquip(event.player, InteractionHand.OFF_HAND);
        }
    }

    private static void tryEquip(Player player, InteractionHand hand) {
        var key = new Pair<>(hand, player);
        var lastSlot = lastSelectedSlots.getOrDefault(key, Pair.of(ItemStack.EMPTY, 0));
        var newItem = player.getItemInHand(hand);

        if (newItem != lastSlot.getFirst() || newItem.getCount() < lastSlot.getSecond()) {
            EquipTracker.stopEquip(player, hand);

            if (newItem.getItem() instanceof IWeapon) {
                var data = new GunData(newItem, player);
                var equipTime = GunModifierHelper.getEquipTime(data);
                EquipTracker.startEquip(player, hand, equipTime);
            }
            else if(newItem.getItem() instanceof IThrowable throwable){
                EquipTracker.startEquip(player, hand, throwable.getConfig().getGeneral().getEquipTime());
            }

            lastSelectedSlots.put(key, Pair.of(newItem, newItem.getCount()));
        }
    }
}
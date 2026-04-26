package com.nukateam.ntgl.common.handlers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.handler.ClientEquipHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class PlayerEventHandler {
    private static final Map<Pair<InteractionHand, Player>, Slot> lastSelectedSlots = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            tryEquip(event.getEntity(), InteractionHand.MAIN_HAND);
            tryEquip(event.getEntity(), InteractionHand.OFF_HAND);
        }
    }

    @SubscribeEvent
    public static void onChangeEquipment(LivingEquipmentChangeEvent event) {
        var oldItem = event.getFrom();
        var newItem = event.getTo();
        if(event.getSlot() == EquipmentSlot.MAINHAND || event.getSlot() == EquipmentSlot.OFFHAND) {
            var hand = getHand(event.getSlot());
            if (newItem.getItem() instanceof IThrowable throwable) {
                if (newItem.getCount() < oldItem.getCount()) {

                    var data = new WeaponData(newItem, event.getEntity());
                    var equipTime = WeaponModifierHelper.getEquipTime(data);
                    EquipTracker.stopEquip(event.getEntity(), hand);
                    EquipTracker.startEquip(event.getEntity(), hand, equipTime);
                }
            }
            else EquipTracker.stopEquip(event.getEntity(), hand);
        }
    }

    private static void tryEquip(Player player, InteractionHand hand) {
        var key = new Pair<>(hand, player);
        var lastSlot = lastSelectedSlots.getOrDefault(key, new Slot(ItemStack.EMPTY, 0, 0));
        var newItem = player.getItemInHand(hand);

        if (newItem.getItem() != lastSlot.stack.getItem() || newItem.getCount() < lastSlot.stackSize()
                || player.getInventory().selected != lastSlot.slotId) {

            if (newItem.getItem() instanceof IThrowable) {
                if (newItem.getCount() < lastSlot.stackSize()) {
                    var data = new WeaponData(newItem, player);
                    var equipTime = WeaponModifierHelper.getEquipTime(data);
                    ClientEquipHandler.get().setEquiping(hand, equipTime);
                }
            }

            lastSelectedSlots.put(key, new Slot(newItem, newItem.getCount(), player.getInventory().selected));
        }
    }

    private static @NotNull InteractionHand getHand(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    record Slot(ItemStack stack, int stackSize, int slotId){}
}
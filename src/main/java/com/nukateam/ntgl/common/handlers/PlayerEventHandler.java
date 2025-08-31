package com.nukateam.ntgl.common.handlers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    public static final UUID SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-5e6f-7890-1234-567890abcdef");
    public static final String MOVEMENT_SPEED = "custom_movement_speed";

    private static final Map<Pair<InteractionHand, Player>, Slot> lastSelectedSlots = new HashMap<>();
    public static final String ID = "WeaponId";

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
    public static void onChangeEquipment(LivingEquipmentChangeEvent event) {
        var oldItem = event.getFrom();
        var newItem = event.getTo();

        var isGun = newItem.getItem() instanceof IWeapon;
        var isThrowable = newItem.getItem() instanceof IThrowable;
        var lastItemId = getId(oldItem);
        var newItemId = getId(newItem);

        if ((isGun || isThrowable) &&
                (!lastItemId.equals(newItemId) || newItem.getCount() < oldItem.getCount())) {
            if(event.getSlot() == EquipmentSlot.MAINHAND || event.getSlot() == EquipmentSlot.OFFHAND) {
                var hand = event.getSlot() == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                var equipTime = 0;

                if (newItem.getItem() instanceof IWeapon) {
                    var data = new GunData(newItem, event.getEntity());
                    equipTime = GunModifierHelper.getEquipTime(data);
                }
                else if(newItem.getItem() instanceof IThrowable throwable){
                    equipTime = throwable.getConfig().getGeneral().getEquipTime();
                }

                newItem.getOrCreateTag().putString(ID, newItemId);
                EquipTracker.startEquip(event.getEntity(), hand, equipTime);
            }
        }
    }

    private static void tryEquip(Player player, InteractionHand hand) {
        var key = new Pair<>(hand, player);
        var lastSlot = lastSelectedSlots.getOrDefault(key, new Slot(ItemStack.EMPTY, 0, 0));
        var newItem = player.getItemInHand(hand);

        var lastItemId = getId(lastSlot.stack);
        var newItemId = getId(newItem);

        var isGun = newItem.getItem() instanceof IWeapon;
        var isThrowable = newItem.getItem() instanceof IThrowable;

        if ((isGun || isThrowable) && !lastItemId.equals(newItemId)
                || newItem.getCount() < lastSlot.stackSize()
                || player.getInventory().selected != lastSlot.slotId) {
            EquipTracker.stopEquip(player, hand);

            var equipTime = 0;
            if (isGun) {
                var data = new GunData(newItem, player);
                equipTime = GunModifierHelper.getEquipTime(data);
                newItem.getOrCreateTag().putString(ID, newItemId);
            }
            else if(newItem.getItem() instanceof IThrowable throwable){
                equipTime = throwable.getConfig().getGeneral().getEquipTime();
                newItem.getOrCreateTag().putString(ID, newItemId);
            }

            EquipTracker.startEquip(player, hand, equipTime);

            lastSelectedSlots.put(key, new Slot(newItem, newItem.getCount(), player.getInventory().selected));
        }
    }

    private static String getId(ItemStack stack) {
        var lastItemTag = stack.getOrCreateTag();
        var lastItemId = lastItemTag.contains(ID, Tag.TAG_STRING) ?
                lastItemTag.getString(ID):
                UUID.randomUUID().toString();
        return lastItemId;
    }

    record Slot(ItemStack stack, int stackSize, int slotId){}
}
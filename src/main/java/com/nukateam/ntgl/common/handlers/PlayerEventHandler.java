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
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    public static final UUID SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-5e6f-7890-1234-567890abcdef");
    public static final String MOVEMENT_SPEED = "custom_movement_speed";

    private static final Map<Pair<InteractionHand, Player>, Slot> lastSelectedSlots = new HashMap<>();
    public static final String ID = "WeaponId";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
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

        if(event.getSlot() == EquipmentSlot.MAINHAND || event.getSlot() == EquipmentSlot.OFFHAND) {
            var hand = getHand(event.getSlot());
            if (newItem.getItem() instanceof IThrowable throwable) {
                if (newItem.getCount() < oldItem.getCount()) {
                    var equipTime = throwable.getConfig().getGeneral().getEquipTime();
                    EquipTracker.stopEquip(event.getEntity(), hand);
                    EquipTracker.startEquip(event.getEntity(), hand, equipTime);
                }
            }
            else EquipTracker.stopEquip(event.getEntity(), hand);
        }
    }

    private static @NotNull InteractionHand getHand(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static String getId(ItemStack stack) {
        var lastItemTag = stack.getOrCreateTag();
        return lastItemTag.contains(ID, Tag.TAG_STRING) ?
                lastItemTag.getString(ID):
                UUID.randomUUID().toString();
    }

    record Slot(ItemStack stack, int stackSize, int slotId){}
}
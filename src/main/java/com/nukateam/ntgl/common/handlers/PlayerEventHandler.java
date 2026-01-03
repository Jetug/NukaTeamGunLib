package com.nukateam.ntgl.common.handlers;

import com.google.common.collect.HashMultimap;
import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.handler.ClientEquipHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;

import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    public static final UUID SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-5e6f-7890-1234-567890abcdef");
    public static final String MOVEMENT_SPEED = "custom_movement_speed";

    private static final Map<Pair<InteractionHand, Player>, Slot> lastSelectedSlots = new HashMap<>();
    public static final String ID = "WeaponId";

    public static Map<UUID, HashMultimap<Attribute, AttributeModifier>> PLAYER_MODIFIERS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            var player = event.player;
            var mods = PLAYER_MODIFIERS.get(player.getUUID());
            if (mods != null) {
                player.getAttributes().removeAttributeModifiers(mods);
            }
            applyAttributeModifiers(player, InteractionHand.MAIN_HAND);
            applyAttributeModifiers(player, InteractionHand.OFF_HAND);
        }
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.CLIENT) {
            tryEquip(event.player, InteractionHand.MAIN_HAND);
            tryEquip(event.player, InteractionHand.OFF_HAND);
        }
    }

    private static void applyAttributeModifiers(Player player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof IWeapon) {
            var modifiers = WeaponModifierHelper.getAttributeModifiers(new WeaponData(heldItem, player));
            var multiMap = HashMultimap.<Attribute, AttributeModifier>create();

            for (var modifier : modifiers) {
                var attribute = ATTRIBUTES.getValue(modifier.getAttribute()); if (attribute == null) continue;
                var attributeInstance = player.getAttribute(attribute); if (attributeInstance == null) continue;
                var name = modifier.getAttribute().toString() + hand;
                var uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
                var newModifier = new AttributeModifier(uuid, name, modifier.getValue(), modifier.getOperation());

                if (!attributeInstance.hasModifier(newModifier)) {
                    attributeInstance.addTransientModifier(newModifier);
                }
                multiMap.put(attribute, newModifier);
            }
            PLAYER_MODIFIERS.put(player.getUUID(), multiMap);
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

    private static String getId(ItemStack stack) {
        var lastItemTag = stack.getOrCreateTag();
        return lastItemTag.contains(ID, Tag.TAG_STRING) ?
                lastItemTag.getString(ID):
                UUID.randomUUID().toString();
    }

    record Slot(ItemStack stack, int stackSize, int slotId){}
}
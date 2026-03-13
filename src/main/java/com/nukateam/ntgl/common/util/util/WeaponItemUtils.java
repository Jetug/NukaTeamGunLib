package com.nukateam.ntgl.common.util.util;

import com.google.common.collect.HashMultimap;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES;

public class WeaponItemUtils {
    public static void checkAmmo(ItemStack stack, Entity entity, LivingEntity livingEntity) {
        var data = new WeaponData(stack, livingEntity);
        var ammoItems = WeaponModifierHelper.getAmmoItems(data);
        var ammoId = WeaponStateHelper.getCurrentAmmo(data).getId();
        var matches = ammoItems.stream().anyMatch((i) -> i.getId().equals(ammoId));

        if(!matches) {
            if (entity instanceof ServerPlayer) {
                ServerPlayHandler.unloadGun(data);
            }
            var firstAmmo = SetUtils.getFirst(ammoItems);
            WeaponStateHelper.setCurrentAmmo(data, firstAmmo.getId());
        }

        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
        var currentAmount = WeaponStateHelper.getAmmoCount(data);
        if(currentAmount > maxAmmo){
            if (entity instanceof ServerPlayer) {
                ServerPlayHandler.unloadGun(data);
            }
        }
    }

    public static void applyAttributeModifiers(LivingEntity player, InteractionHand hand) {
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
            WeaponItem.PLAYER_MODIFIERS.put(player.getUUID(), multiMap);
        }
    }
}

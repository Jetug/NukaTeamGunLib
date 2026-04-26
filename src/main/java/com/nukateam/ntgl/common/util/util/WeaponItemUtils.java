package com.nukateam.ntgl.common.util.util;

import com.google.common.collect.HashMultimap;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
}

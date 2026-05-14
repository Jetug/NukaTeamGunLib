package com.nukateam.ntgl.common.handlers;

import com.google.common.collect.HashMultimap;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityEvents {
    public static Map<UUID, HashMultimap<Attribute, AttributeModifier>> PLAYER_MODIFIERS = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();

        if (!entity.level().isClientSide){
            DeathEffect.createDeathEffect(entity, event.getSource());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        var mods = PLAYER_MODIFIERS.get(entity.getUUID());
        if (mods != null) {
            entity.getAttributes().removeAttributeModifiers(mods);
        }
        applyAttributeModifiers(entity, InteractionHand.MAIN_HAND);
        applyAttributeModifiers(entity, InteractionHand.OFF_HAND);
    }

    private static void applyAttributeModifiers(LivingEntity player, InteractionHand hand) {
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
}

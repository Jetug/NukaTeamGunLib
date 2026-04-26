package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityEvents {
    public static Map<UUID, HashMultimap<Holder<Attribute>, AttributeModifier>> PLAYER_MODIFIERS = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();

        if (!entity.level().isClientSide){
            DeathEffect.createDeathEffect(entity, event.getSource());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        var entity = event.getEntity();
        if(entity instanceof LivingEntity livingEntity){
            var mods = PLAYER_MODIFIERS.get(entity.getUUID());
            if (mods != null) {
                livingEntity.getAttributes().removeAttributeModifiers(mods);
            }
            applyAttributeModifiers(livingEntity, InteractionHand.MAIN_HAND);
            applyAttributeModifiers(livingEntity, InteractionHand.OFF_HAND);
        }
    }

    private static void applyAttributeModifiers(LivingEntity player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof IWeapon) {
            var modifiers = WeaponModifierHelper.getAttributeModifiers(new WeaponData(heldItem, player));
            var multiMap = HashMultimap.<Holder<Attribute>, AttributeModifier>create();

            for (var modifier : modifiers) {
                BuiltInRegistries.ATTRIBUTE.getHolder(modifier.getAttribute()).ifPresent((attribute -> {
                    var attributeInstance = player.getAttribute(attribute);
                    if (attributeInstance != null) {
                        var name = modifier.getAttribute().toString().replace(".", "_") + "_" + hand.toString().toLowerCase(Locale.ROOT);
                        var id = ResourceLocation.parse(name);
                        var newModifier = new AttributeModifier(id, modifier.getValue(), modifier.getOperation());

                        if (!attributeInstance.hasModifier(id)) {
                            attributeInstance.addTransientModifier(newModifier);
                        }
                        multiMap.put(attribute, newModifier);
                    }
                }));

            }
            PLAYER_MODIFIERS.put(player.getUUID(), multiMap);
        }
    }
}

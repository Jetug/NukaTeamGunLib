package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Jetug
 */
@EventBusSubscriber
public class NtglDamageTypes {
    public static final ResourceKey<DamageType> BULLET = create("bullet");
    public static final ResourceKey<DamageType> ENERGY = create("energy");
    public static final ResourceKey<DamageType> EXPLOSIVE = create("explosive");
    public static final ResourceKey<DamageType> FIRE = create("fire");

    @NotNull
    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryBuild(Ntgl.MOD_ID, name));
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.DAMAGE_TYPE, helper -> {
            helper.register(BULLET, new DamageType("bullet", 0.1f));
            helper.register(ENERGY, new DamageType("energy", 0.1f));
            helper.register(EXPLOSIVE, new DamageType("explosive", 0.1f));
            helper.register(FIRE, new DamageType("fire", 0.1f));
        });
    }
}

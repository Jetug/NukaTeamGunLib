package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Jetug
 */
public class NtglDamageTypes {
    public static final ResourceKey<DamageType> BULLET = create("bullet");
    public static final ResourceKey<DamageType> ENERGY = create("energy");
    public static final ResourceKey<DamageType> EXPLOSIVE = create("explosive");
    public static final ResourceKey<DamageType> FIRE = create("fire");

    @NotNull
    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryBuild(Ntgl.MOD_ID, name));
    }

    public static void bootstrap(BootstapContext<DamageType> bootstapContext) {
        bootstapContext.register(BULLET, new DamageType("bullet", 0.1f));
        bootstapContext.register(ENERGY, new DamageType("energy", 0.1f));
        bootstapContext.register(EXPLOSIVE, new DamageType("explosive", 0.1f));
        bootstapContext.register(FIRE, new DamageType("fire", 0.1f));
    }
}

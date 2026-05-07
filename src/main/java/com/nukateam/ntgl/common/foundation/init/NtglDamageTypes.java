package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Author: Jetug
 */
public class NtglDamageTypes {
    public static final ResourceKey<DamageType> BULLET = create("bullet");
    public static final ResourceKey<DamageType> ENERGY = create("energy");
    public static final ResourceKey<DamageType> EXPLOSIVE = create("explosive");
    public static final ResourceKey<DamageType> FIRE = create("fire");

    public static void bootstrap(BootstrapContext<DamageType> bootstrap) {
        bootstrap.register(BULLET,new DamageType("bullet", 0.1f));
        bootstrap.register(ENERGY,new DamageType("energy", 0.1f));
        bootstrap.register(EXPLOSIVE,new DamageType("explosive", 0.1f));
        bootstrap.register(FIRE,new DamageType("fire", 0.1f));

    }

    @NotNull
    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, name));
    }

}

package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.config.weapon.General;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NtglEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Ntgl.MOD_ID);

    public static final EntityDataSerializer<ProjectileConfig> PROJECTILE_CONFIG_SERIALIZER =
            EntityDataSerializer.forValueType(
                    StreamCodec.of(
                            NtglEntityDataSerializers::writeProjectile,
                            NtglEntityDataSerializers::readProjectile
                    )
            );

    public static final EntityDataSerializer<General> GENERAL_CONFIG_SERIALIZER =
            EntityDataSerializer.forValueType(
                    StreamCodec.of(
                            NtglEntityDataSerializers::writeGeneral,
                            NtglEntityDataSerializers::readGeneral
                    )
            );

    public static final Supplier<EntityDataSerializer<ProjectileConfig>> PROJECTILE_CONFIG =
            SERIALIZERS.register("projectile_config", () -> PROJECTILE_CONFIG_SERIALIZER);

    public static final Supplier<EntityDataSerializer<General>> GENERAL_CONFIG =
            SERIALIZERS.register("general_config", () -> GENERAL_CONFIG_SERIALIZER);

    private static void writeProjectile(RegistryFriendlyByteBuf buf, ProjectileConfig config) {
        CompoundTag tag = config.serializeNBT(buf.registryAccess());
        buf.writeNbt(tag);
    }

    private static ProjectileConfig readProjectile(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();

        if(tag == null)
            return new ProjectileConfig();

        return ProjectileConfig.create(tag);
    }

    private static void writeGeneral(RegistryFriendlyByteBuf buf, General config) {
        CompoundTag tag = config.serializeNBT(buf.registryAccess());
        buf.writeNbt(tag);
    }

    private static General readGeneral(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();

        if(tag == null)
            return new General();

        return General.create(tag);
    }

    public static void register(IEventBus bus){
        SERIALIZERS.register(bus);
    }
}

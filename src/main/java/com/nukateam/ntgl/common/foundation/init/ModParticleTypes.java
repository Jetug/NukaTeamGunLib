package com.nukateam.ntgl.common.foundation.init;

import com.mojang.serialization.MapCodec;
import com.nukateam.ntgl.common.foundation.particles.*;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


/**
 * Author: MrCrayfish
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<BulletHoleData>> BULLET_HOLE =
            REGISTER.register("bullet_hole",
                    () -> new ParticleType<>(false) {
                        @Override
                        public MapCodec<BulletHoleData> codec() {
                            return BulletHoleData.CODEC;
                        }
                        @Override
                        public StreamCodec<FriendlyByteBuf, BulletHoleData> streamCodec() {
                            return BulletHoleData.CODEC;
                        }
                    });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD =
            REGISTER.register("blood", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, ParticleType<TrailData>> TRAIL =
            REGISTER.register("trail",
                    () -> new ParticleType<>(false) {
                        @Override
                        public MapCodec<TrailData> codec() {
                            return TrailData.CODEC;
                        }
                        @Override
                        public StreamCodec<FriendlyByteBuf, TrailData> streamCodec() {
                            return TrailData.CODEC;
                        }
                    });
}

package com.nukateam.ntgl.common.foundation.particles;

import com.mojang.serialization.MapCodec;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Author: MrCrayfish
 */
public class TrailData implements ParticleOptions {
    public static final MapCodec<TrailData> CODEC =
            RecordCodecBuilder.mapCodec(b -> b.group(
                    Codec.BOOL.fieldOf("enchanted").forGetter(TrailData::isEnchanted)
            ).apply(b, TrailData::new));

    public static final StreamCodec<FriendlyByteBuf, TrailData> CODEC =
            StreamCodec.of(
                    (buf, data) -> buf.writeBoolean(data.enchanted),
                    buf -> new TrailData(buf.readBoolean())
            );

    private final boolean enchanted;

    public TrailData(boolean enchanted) { this.enchanted = enchanted; }
    public boolean isEnchanted() { return enchanted; }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.TRAIL.get();
    }
}

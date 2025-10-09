package com.nukateam.ntgl.common.foundation.particles;


import com.mojang.serialization.MapCodec;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Author: MrCrayfish
 */


public record BulletHoleData(Direction direction, BlockPos pos) implements ParticleOptions {
    public static final MapCodec<BulletHoleData> CODEC =
            RecordCodecBuilder.mapCodec(b -> b.group(
                    Direction.CODEC.fieldOf("dir").forGetter(BulletHoleData::direction),
                    BlockPos.CODEC.fieldOf("pos").forGetter(BulletHoleData::pos)
            ).apply(b, BulletHoleData::new));

    public static final StreamCodec<FriendlyByteBuf, BulletHoleData> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeEnum(data.direction);
                        buf.writeBlockPos(data.pos);
                    },
                    buf -> new BulletHoleData(buf.readEnum(Direction.class), buf.readBlockPos())
            );

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.BULLET_HOLE.get();
    }
}

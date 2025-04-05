package com.nukateam.ntgl.common.foundation.particles;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
public class BulletHoleData implements ParticleOptions {
    public static final Codec<BulletHoleData> CODEC = RecordCodecBuilder.create(BulletHoleData::apply);
    public static final ParticleOptions.Deserializer<BulletHoleData> DESERIALIZER = new BulletHoleDeserializer();

    private final Direction direction;
    private final BlockPos pos;

    public BulletHoleData(int dir, long pos) {
        this.direction = Direction.values()[dir];
        this.pos = BlockPos.of(pos);
    }

    public BulletHoleData(Direction dir, BlockPos pos) {
        this.direction = dir;
        this.pos = pos;
    }

    public static Codec<BulletHoleData> codec(ParticleType<BulletHoleData> type) {
        return CODEC;
    }

    static class BulletHoleDeserializer implements ParticleOptions.Deserializer<BulletHoleData>{
        @Override
        public BulletHoleData fromCommand(ParticleType<BulletHoleData> particleType, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            int dir = reader.readInt();
            reader.expect(' ');
            long pos = reader.readLong();
            return new BulletHoleData(dir, pos);
        }

        @Override
        public BulletHoleData fromNetwork(ParticleType<BulletHoleData> particleType, FriendlyByteBuf buffer) {
            return new BulletHoleData(buffer.readInt(), buffer.readLong());
        }
    }

    private static App<Mu<BulletHoleData>, BulletHoleData> apply(RecordCodecBuilder.Instance<BulletHoleData> builder) {
        return builder.group(
                Codec.INT.fieldOf("dir").forGetter(BulletHoleData::getDirFromData),
                Codec.LONG.fieldOf("pos").forGetter(BulletHoleData::getPosFromData)
        ).apply(builder, BulletHoleData::new);
    }

    private static Integer getDirFromData(BulletHoleData data) {
        return data.direction.ordinal();
    }

    private static Long getPosFromData(BulletHoleData data) {
        return data.pos.asLong();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.BULLET_HOLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.direction);
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + this.direction.getName();
    }


}

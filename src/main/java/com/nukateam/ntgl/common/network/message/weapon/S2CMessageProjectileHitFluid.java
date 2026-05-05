package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitFluid implements CustomPacketPayload {
    public static final Type<S2CMessageProjectileHitFluid> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_projectile_hit_fluid"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageProjectileHitFluid> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private Vec3 pos;
    float size;
    float speed;
    boolean isInLava;
    private int projectileId;

    public S2CMessageProjectileHitFluid() {}

    public S2CMessageProjectileHitFluid(Vec3 pos, float size, float speed, boolean isInLava, int projectileId) {
        this.pos = pos;
        this.size = size;
        this.speed = speed;
        this.isInLava = isInLava;
        this.projectileId = projectileId;
    }

    public static void encode(S2CMessageProjectileHitFluid message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.pos));
        buffer.writeFloat(message.size);
        buffer.writeFloat(message.speed);
        buffer.writeBoolean(message.isInLava);
        buffer.writeInt(message.projectileId);
    }

    public static S2CMessageProjectileHitFluid decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        return new S2CMessageProjectileHitFluid(
                pos,
                buffer.readFloat  (),
                buffer.readFloat  (),
                buffer.readBoolean(),
                buffer.readInt    ()
        );
    }

    public static void handle(S2CMessageProjectileHitFluid message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleProjectileHitFluid(message)));
    }

    public Vec3 getPos() {
        return pos;
    }

    public float getSize() {
        return size;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isInLava() {
        return isInLava;
    }

    public int getProjectileId() {
        return projectileId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitBlock implements CustomPacketPayload {
    public static final Type<S2CMessageProjectileHitBlock> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_projectile_hit_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageProjectileHitBlock> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private Vec3 hitPos;
    private BlockPos blockPos;
    private Direction face;

    public S2CMessageProjectileHitBlock() {}

    public S2CMessageProjectileHitBlock(Vec3 hitPos, BlockPos blockPos, Direction face) {
        this.hitPos = hitPos;
        this.blockPos = blockPos;
        this.face = face;
    }

    public static void encode(S2CMessageProjectileHitBlock message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.hitPos));
        buffer.writeBlockPos(message.blockPos);
        buffer.writeEnum(message.face);
    }

    public static S2CMessageProjectileHitBlock decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        var blockPos = buffer.readBlockPos();
        var face = buffer.readEnum(Direction.class);
        return new S2CMessageProjectileHitBlock(pos, blockPos, face);
    }

    public static void handle(S2CMessageProjectileHitBlock message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleProjectileHitBlock(message)));
    }

    public Vec3 getHitPos() {
        return this.hitPos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public Direction getFace() {
        return this.face;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

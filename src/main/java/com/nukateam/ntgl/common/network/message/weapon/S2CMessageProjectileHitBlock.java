package com.nukateam.ntgl.common.network.message.weapon;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitBlock implements IMessage<S2CMessageProjectileHitBlock> {
    private Vec3 hitPos;
    private BlockPos blockPos;
    private Direction face;

    public S2CMessageProjectileHitBlock() {}

    public S2CMessageProjectileHitBlock(Vec3 hitPos, BlockPos blockPos, Direction face) {
        this.hitPos = hitPos;
        this.blockPos = blockPos;
        this.face = face;
    }

    @Override
    public void encode(S2CMessageProjectileHitBlock message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.hitPos));
        buffer.writeBlockPos(message.blockPos);
        buffer.writeEnum(message.face);
    }

    @Override
    public S2CMessageProjectileHitBlock decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        var blockPos = buffer.readBlockPos();
        var face = buffer.readEnum(Direction.class);
        return new S2CMessageProjectileHitBlock(pos, blockPos, face);
    }

    @Override
    public void handle(S2CMessageProjectileHitBlock message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleProjectileHitBlock(message)));
        supplier.setPacketHandled(true);
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
}

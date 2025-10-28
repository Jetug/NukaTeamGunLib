package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitBlock extends PlayMessage<S2CMessageProjectileHitBlock> {
    private double x;
    private double y;
    private double z;
    private BlockPos pos;
    private Direction face;

    public S2CMessageProjectileHitBlock() {}

    public S2CMessageProjectileHitBlock(Vec3 hitPos, BlockPos blockPos, Direction face) {
        this.x = hitPos.x;
        this.y = hitPos.y;
        this.z = hitPos.z;
        this.pos = blockPos;
        this.face = face;
    }

    @Override
    public void encode(S2CMessageProjectileHitBlock message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeBlockPos(message.pos);
        buffer.writeEnum(message.face);
    }

    @Override
    public S2CMessageProjectileHitBlock decode(FriendlyByteBuf buffer) {
        var x = buffer.readDouble();
        var y = buffer.readDouble();
        var z = buffer.readDouble();
        var pos = buffer.readBlockPos();
        var face = buffer.readEnum(Direction.class);
        return new S2CMessageProjectileHitBlock(new Vec3(x, y, z), pos, face);
    }

    @Override
    public void handle(S2CMessageProjectileHitBlock message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleProjectileHitBlock(message)));
        supplier.setHandled(true);
    }

    public Vec3 getHitPos() {
        return new Vec3(x, y, z);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getFace() {
        return this.face;
    }
}

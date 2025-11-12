package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitFluid extends PlayMessage<S2CMessageProjectileHitFluid> {
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

    @Override
    public void encode(S2CMessageProjectileHitFluid message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.pos));
        buffer.writeFloat(message.size);
        buffer.writeFloat(message.speed);
        buffer.writeBoolean(message.isInLava);
        buffer.writeInt(message.projectileId);
    }

    @Override
    public S2CMessageProjectileHitFluid decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        return new S2CMessageProjectileHitFluid(
                pos,
                buffer.readFloat  (),
                buffer.readFloat  (),
                buffer.readBoolean(),
                buffer.readInt    ()
        );
    }

    @Override
    public void handle(S2CMessageProjectileHitFluid message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleProjectileHitFluid(message)));
        supplier.setHandled(true);
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
}

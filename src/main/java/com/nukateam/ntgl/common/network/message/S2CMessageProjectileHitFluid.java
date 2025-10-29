package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class S2CMessageProjectileHitFluid extends PlayMessage<S2CMessageProjectileHitFluid> {
    private Vec3 pos;
    private int projectileId;

    public S2CMessageProjectileHitFluid() {}

    public S2CMessageProjectileHitFluid(Vec3 pos, int projectileId) {
        this.pos = pos;
        this.projectileId = projectileId;
    }

    @Override
    public void encode(S2CMessageProjectileHitFluid message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.pos));
        buffer.writeInt(message.projectileId);
    }

    @Override
    public S2CMessageProjectileHitFluid decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        return new S2CMessageProjectileHitFluid(pos, buffer.readInt());
    }

    @Override
    public void handle(S2CMessageProjectileHitFluid message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleProjectileHitFluid(message)));
        supplier.setHandled(true);
    }

    public Vec3 getPos() {
        return pos;
    }

    public int getProjectileId() {
        return projectileId;
    }
}

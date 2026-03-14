package com.nukateam.ntgl.common.network.message.weapon;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class S2CMessageProjectileExplosion implements IMessage<S2CMessageProjectileExplosion> {
    private Vec3 position;
    private Vec3 knockback;
    private ExplosionConfig config;
    private List<BlockPos> toBlow;

    public S2CMessageProjectileExplosion() {}

    public S2CMessageProjectileExplosion(Vec3 position, Vec3 knockback, ExplosionConfig config, List<BlockPos> toBlow) {
        this.position = position;
        this.knockback = Objects.requireNonNullElseGet(knockback, () -> new Vec3(0, 0, 0));
        if(knockback == null){
            this.knockback = Vec3.ZERO;
        }
        this.config = config;
        this.toBlow = toBlow;
    }

    @Override
    public void encode(S2CMessageProjectileExplosion message, FriendlyByteBuf buffer) {
        NbtUtils.writeVec3(buffer, message.position);
        NbtUtils.writeVec3(buffer, message.knockback);
        buffer.writeNbt(message.config.serializeNBT());
        buffer.writeCollection(message.toBlow, (buf, blockPos) -> {
            int x = blockPos.getX() - Mth.floor(message.position.x);
            int y = blockPos.getY() - Mth.floor(message.position.y);
            int z = blockPos.getZ() - Mth.floor(message.position.z);
            buf.writeByte(x);
            buf.writeByte(y);
            buf.writeByte(z);
        });
    }

    @Override
    public S2CMessageProjectileExplosion decode(FriendlyByteBuf buffer) {
        position = NbtUtils.readVec3(buffer);
        knockback = NbtUtils.readVec3(buffer);
        config = ExplosionConfig.create(buffer.readNbt());
        int x = Mth.floor(this.position.x);
        int y = Mth.floor(this.position.y);
        int z = Mth.floor(this.position.z);
        toBlow = buffer.readList((p_178850_) -> {
            int l  = p_178850_.readByte() + x;
            int i1 = p_178850_.readByte() + y;
            int j1 = p_178850_.readByte() + z;
            return new BlockPos(l, i1, j1);
        });

        return new S2CMessageProjectileExplosion(position, knockback, config, toBlow);
    }

    @Override
    public void handle(S2CMessageProjectileExplosion message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleMessageExplosion(message)));
        supplier.setPacketHandled(true);
    }

    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getKnockback() {
        return knockback;
    }

    public ExplosionConfig getConfig() {
        return config;
    }

    public List<BlockPos> getToBlow() {
        return toBlow;
    }
}

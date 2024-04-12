package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.base.network.ServerPlayHandler;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

/**
 * Author: MrCrayfish
 */
public class C2SRequestEntityData extends PlayMessage<C2SRequestEntityData> {
    private int entityId;

    public C2SRequestEntityData() {}

    public C2SRequestEntityData(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void encode(C2SRequestEntityData messageShoot, FriendlyByteBuf buffer) {
        buffer.writeInt(messageShoot.entityId);
    }

    @Override
    public C2SRequestEntityData decode(FriendlyByteBuf buffer) {
        return new C2SRequestEntityData(buffer.readInt());
    }

    @Override
    public void handle(C2SRequestEntityData message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer();
            var level = player.level();
            var entity = level.getEntity(message.getEntityId());

            if (entity instanceof ProjectileEntity projectile)
                projectile.updateClient();
        }));
        supplier.setHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }
}
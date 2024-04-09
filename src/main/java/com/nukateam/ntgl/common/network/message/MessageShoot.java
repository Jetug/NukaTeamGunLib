package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.base.network.ServerPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageShoot extends PlayMessage<MessageShoot> {
    private int shooterId;
    private float rotationYaw;
    private float rotationPitch;
    private float randP;
    private float randY;
    private boolean isMainHand;

    public MessageShoot() {}

    public MessageShoot(int shooterId, float yaw, float pitch, float randP, float randY, boolean isMainHand) {
        this.shooterId = shooterId;
        this.rotationPitch = pitch;
        this.rotationYaw = yaw;
        this.randP = randP;
        this.randY = randY;
        this.isMainHand = isMainHand;
    }

    @Override
    public void encode(MessageShoot messageShoot, FriendlyByteBuf buffer) {
        buffer.writeInt(messageShoot.shooterId);
        buffer.writeFloat(messageShoot.rotationYaw);
        buffer.writeFloat(messageShoot.rotationPitch);
        buffer.writeFloat(messageShoot.randP);
        buffer.writeFloat(messageShoot.randY);
        buffer.writeBoolean(messageShoot.isMainHand);
    }

    @Override
    public MessageShoot decode(FriendlyByteBuf buffer) {
        return new MessageShoot(
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean());
    }

    @Override
    public void handle(MessageShoot messageShoot, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer();
            if (player != null) {
                var shooter = player.level().getEntity(messageShoot.shooterId);

                if (shooter instanceof LivingEntity livingEntity)
                    ServerPlayHandler.handleShoot(messageShoot, livingEntity);
            }
        }));
        supplier.setHandled(true);
    }

    public boolean isMainHand() {
        return isMainHand;
    }

    public float getRotationYaw() {
        return this.rotationYaw;
    }

    public float getRotationPitch() {
        return this.rotationPitch;
    }
}
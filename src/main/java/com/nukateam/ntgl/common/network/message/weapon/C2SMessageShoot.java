package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class C2SMessageShoot  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageShoot> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private int shooterId;
    private float rotationYaw;
    private float rotationPitch;
    private float randP;
    private float randY;
    private InteractionHand hand;
    WeaponMode action;

    public C2SMessageShoot() {}

    public C2SMessageShoot(int shooterId, float yaw, float pitch, float randP, float randY, InteractionHand hand, WeaponMode action) {
        this.shooterId = shooterId;
        this.rotationPitch = pitch;
        this.rotationYaw = yaw;
        this.randP = randP;
        this.randY = randY;
        this.hand = hand;
        this.action = action;
    }

    public static void encode(C2SMessageShoot messageShoot, FriendlyByteBuf buffer) {
        buffer.writeInt(messageShoot.shooterId);
        buffer.writeFloat(messageShoot.rotationYaw);
        buffer.writeFloat(messageShoot.rotationPitch);
        buffer.writeFloat(messageShoot.randP);
        buffer.writeFloat(messageShoot.randY);
        buffer.writeEnum(messageShoot.hand);
        buffer.writeUtf(messageShoot.action.toString());
    }

    public static C2SMessageShoot decode(FriendlyByteBuf buffer) {
        return new C2SMessageShoot(
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageShoot messageShoot, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer().get();
            if (player != null) {
                var shooter = player.level().getEntity(messageShoot.shooterId);

                if (shooter instanceof LivingEntity livingEntity)
                    ServerPlayHandler.handleShoot(messageShoot, livingEntity);
            }
        }));
        supplier.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getMode() {
        return action;
    }

    public float getRotationYaw() {
        return this.rotationYaw;
    }

    public float getRotationPitch() {
        return this.rotationPitch;
    }
}
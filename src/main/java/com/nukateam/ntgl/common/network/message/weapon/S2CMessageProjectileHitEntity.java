package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class S2CMessageProjectileHitEntity implements CustomPacketPayload {
    public static final Type<S2CMessageProjectileHitEntity> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_projectile_hit_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageProjectileHitEntity> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private double x;
    private double y;
    private double z;
    private int type;
    private boolean player;

    public S2CMessageProjectileHitEntity() {}

    public S2CMessageProjectileHitEntity(double x, double y, double z, int type, boolean player) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.player = player;
    }

    public static void encode(S2CMessageProjectileHitEntity message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeByte(message.type);
        buffer.writeBoolean(message.player);
    }

    public static S2CMessageProjectileHitEntity decode(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        byte type = buffer.readByte();
        boolean player = buffer.readBoolean();
        return new S2CMessageProjectileHitEntity(x, y, z, type, player);
    }

    public static void handle(S2CMessageProjectileHitEntity message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleProjectileHitEntity(message)));
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public boolean isHeadshot() {
        return this.type == HitType.HEADSHOT;
    }

    public boolean isCritical() {
        return this.type == HitType.CRITICAL;
    }

    public boolean isPlayer() {
        return this.player;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class HitType {
        public static final int NORMAL = 0;
        public static final int HEADSHOT = 1;
        public static final int CRITICAL = 2;
    }
}

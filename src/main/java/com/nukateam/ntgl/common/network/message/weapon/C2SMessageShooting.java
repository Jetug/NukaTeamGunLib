package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class C2SMessageShooting implements CustomPacketPayload {
    public static final Type<C2SMessageShooting> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_shooting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageShooting> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private boolean shooting;

    public C2SMessageShooting() {}

    public C2SMessageShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public static void encode(C2SMessageShooting message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.shooting);
    }

    public static C2SMessageShooting decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooting(buffer.readBoolean());
    }

    public static void handle(C2SMessageShooting message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> {
            var player = supplier.player();
            if (player != null) {
                ModSyncedDataKeys.SHOOTING_RIGHT.setValue(player, message.shooting);
            }
        }));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

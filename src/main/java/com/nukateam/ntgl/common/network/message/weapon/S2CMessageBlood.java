package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood implements CustomPacketPayload {
    public static final Type<S2CMessageBlood> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_blood"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageBlood> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private Vec3 pos;

    public S2CMessageBlood() {}

    public S2CMessageBlood(Vec3 pos) {
        this.pos = pos;
    }

    public static void encode(S2CMessageBlood message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.pos));
    }

    public static S2CMessageBlood decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        return new S2CMessageBlood(pos);
    }

    public static void handle(S2CMessageBlood message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleMessageBlood(message)));
    }

    public Vec3 getPos() {
        return pos;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

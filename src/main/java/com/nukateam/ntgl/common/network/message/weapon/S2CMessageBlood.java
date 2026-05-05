package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood  {
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

    public static void handle(S2CMessageBlood message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleMessageBlood(message)));
        supplier.setHandled(true);
    }

    public Vec3 getPos() {
        return pos;
    }
}

package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.IMessage;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood implements IMessage<S2CMessageBlood> {
    private Vec3 pos;

    public S2CMessageBlood() {}

    public S2CMessageBlood(Vec3 pos) {
        this.pos = pos;
    }

    @Override
    public void encode(S2CMessageBlood message, FriendlyByteBuf buffer) {
        buffer.writeNbt(NbtUtils.writeVec3(message.pos));
    }

    @Override
    public S2CMessageBlood decode(FriendlyByteBuf buffer) {
        var pos = NbtUtils.readVec3(buffer.readNbt());
        return new S2CMessageBlood(pos);
    }

    @Override
    public void handle(S2CMessageBlood message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleMessageBlood(message)));
        supplier.setPacketHandled(true);
    }

    public Vec3 getPos() {
        return pos;
    }
}

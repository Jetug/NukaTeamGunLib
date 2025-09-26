package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class S2CMessageReload extends PlayMessage<S2CMessageReload> {
    private boolean reload;
    private boolean isRightHand;

    public S2CMessageReload() {}

    public S2CMessageReload(boolean reload, InteractionHand arm) {
        this.reload = reload;
        this.isRightHand = arm == InteractionHand.MAIN_HAND;
    }

    public S2CMessageReload(boolean reload, boolean isRightHand) {
        this.reload = reload;
        this.isRightHand = isRightHand;
    }

    @Override
    public void encode(S2CMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.reload);
        buffer.writeBoolean(message.isRightHand);
    }

    @Override
    public S2CMessageReload decode(FriendlyByteBuf buffer) {
        return new S2CMessageReload(
                buffer.readBoolean(),
                buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageReload message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleReload(message)));
        supplier.setHandled(true);
    }

    public boolean isReload() {
        return reload;
    }

    public boolean isRightHand() {
        return isRightHand;
    }
}

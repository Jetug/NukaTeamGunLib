package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;

public class S2CMessageSwitchFireMode extends PlayMessage<S2CMessageSwitchFireMode> {
    private boolean isRightHand = true;

    public S2CMessageSwitchFireMode() {}

    public S2CMessageSwitchFireMode(HumanoidArm arm) {
        this.isRightHand = arm == HumanoidArm.RIGHT;
    }

    public S2CMessageSwitchFireMode(boolean isRightHand) {
        this.isRightHand = isRightHand;
    }

    @Override
    public void encode(S2CMessageSwitchFireMode message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isRightHand);
    }

    @Override
    public S2CMessageSwitchFireMode decode(FriendlyByteBuf buffer) {
        return new S2CMessageSwitchFireMode(buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageSwitchFireMode message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleFireModeSwitch(message)));
        supplier.setHandled(true);
    }

    public boolean isRightHand() {
        return isRightHand;
    }
}

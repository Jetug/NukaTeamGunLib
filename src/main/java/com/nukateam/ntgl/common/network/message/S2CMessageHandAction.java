package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.ClientPlayHandler;
import com.nukateam.ntgl.common.network.HandAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;

public class S2CMessageHandAction extends PlayMessage<S2CMessageHandAction> {
    private boolean isRightHand = true;
    private HandAction handAction;

    public S2CMessageHandAction() {}

    public S2CMessageHandAction(HumanoidArm arm, HandAction handAction) {
        this(arm == HumanoidArm.RIGHT, handAction);
    }

    public S2CMessageHandAction(boolean isRightHand, HandAction handAction) {
        this.isRightHand = isRightHand;
        this.handAction = handAction;
    }

    @Override
    public void encode(S2CMessageHandAction message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isRightHand);
        buffer.writeEnum(message.handAction);
    }

    @Override
    public S2CMessageHandAction decode(FriendlyByteBuf buffer) {
        return new S2CMessageHandAction(buffer.readBoolean(), buffer.readEnum(HandAction.class));
    }

    @Override
    public void handle(S2CMessageHandAction message, MessageContext supplier) {
        supplier.execute((() -> {
            switch (message.handAction){
                case SWITCH_FIRE_MODE -> ClientPlayHandler.handleFireModeSwitch(message);
                case SWITCH_AMMO -> ClientPlayHandler.handleAmmoModeSwitch(message);
            }

        }));
        supplier.setHandled(true);
    }

    public boolean isRightHand() {
        return isRightHand;
    }
}

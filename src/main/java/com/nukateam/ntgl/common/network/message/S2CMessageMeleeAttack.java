package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.network.HandAction;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class S2CMessageMeleeAttack extends PlayMessage<S2CMessageMeleeAttack> {
    public S2CMessageMeleeAttack() {}

    @Override
    public void encode(S2CMessageMeleeAttack message, FriendlyByteBuf buffer) {}

    @Override
    public S2CMessageMeleeAttack decode(FriendlyByteBuf buffer) {
        return new S2CMessageMeleeAttack();
    }

    @Override
    public void handle(S2CMessageMeleeAttack message, MessageContext context) {
        context.execute(() -> {
            var player = context.getPlayer();
            if (player != null) {
                ServerPlayHandler.handleMeleeAttack(message, player);
            }
        });
        context.setHandled(true);
    }
}

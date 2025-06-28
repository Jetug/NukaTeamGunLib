package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

public class C2SMessageMeleeAttack extends PlayMessage<C2SMessageMeleeAttack> {
    public C2SMessageMeleeAttack() {}

    @Override
    public void encode(C2SMessageMeleeAttack message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageMeleeAttack decode(FriendlyByteBuf buffer) {
        return new C2SMessageMeleeAttack();
    }

    @Override
    public void handle(C2SMessageMeleeAttack message, MessageContext context) {
        context.execute(() -> {
            var player = context.getPlayer();
            if (player != null) {
                ServerPlayHandler.handleMeleeAttack(message, player);
            }
        });
        context.setHandled(true);
    }
}

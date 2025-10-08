package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.data.config.WeaponAction;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class C2SMessageMeleeAttack extends PlayMessage<C2SMessageMeleeAttack> {
    InteractionHand hand;
    WeaponAction action;

    public C2SMessageMeleeAttack() {}

    public C2SMessageMeleeAttack(InteractionHand hand, WeaponAction action) {
        this.hand = hand;
        this.action = action;
    }

    @Override
    public void encode(C2SMessageMeleeAttack message, FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeEnum(action);
    }

    @Override
    public C2SMessageMeleeAttack decode(FriendlyByteBuf buffer) {
        return new C2SMessageMeleeAttack(
                buffer.readEnum(InteractionHand.class),
                buffer.readEnum(WeaponAction.class)
        );
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

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponAction getAction() {
        return action;
    }
}

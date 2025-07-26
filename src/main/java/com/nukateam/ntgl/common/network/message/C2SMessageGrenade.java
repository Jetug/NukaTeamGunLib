package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.network.KeyAction;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class C2SMessageGrenade extends PlayMessage<C2SMessageGrenade> {
    private KeyAction action;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageGrenade() {}

    public C2SMessageGrenade(KeyAction reload, InteractionHand hand) {
        this.action = reload;
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessageGrenade message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageGrenade decode(FriendlyByteBuf buffer) {
        return new C2SMessageGrenade(buffer.readEnum(KeyAction.class), buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageGrenade message, MessageContext supplier) {
        supplier.execute((() ->
        {
            ServerPlayer player = supplier.getPlayer();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleGrenade(message, player);
            }
        }));
        supplier.setHandled(true);
    }

    public KeyAction getAction() {
        return action;
    }

    public InteractionHand getHand() {
        return hand;
    }
}

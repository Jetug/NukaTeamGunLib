package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

public class C2SMessageMeleeAttack  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageMeleeAttack> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    InteractionHand hand;
    WeaponMode action;

    public C2SMessageMeleeAttack() {}

    public C2SMessageMeleeAttack(InteractionHand hand, WeaponMode action) {
        this.hand = hand;
        this.action = action;
    }

    public static void encode(C2SMessageMeleeAttack message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.action.toString());
    }

    public static C2SMessageMeleeAttack decode(FriendlyByteBuf buffer) {
        return new C2SMessageMeleeAttack(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf())
        );
    }

    public static void handle(C2SMessageMeleeAttack message, MessageContext context) {
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

    public WeaponMode getAction() {
        return action;
    }
}

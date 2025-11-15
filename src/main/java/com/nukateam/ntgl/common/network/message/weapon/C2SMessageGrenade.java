package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.enums.KeyAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class C2SMessageGrenade  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageGrenade> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private KeyAction action;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    WeaponMode weaponMode;

    public C2SMessageGrenade() {}

    public C2SMessageGrenade(KeyAction reload, InteractionHand hand, WeaponMode weaponMode) {
        this.action = reload;
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageGrenade message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());

    }

    public static C2SMessageGrenade decode(FriendlyByteBuf buffer) {
        return new C2SMessageGrenade(
                buffer.readEnum(KeyAction.class),
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageGrenade message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = supplier.getPlayer().get();
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

    public WeaponMode getAttackMode() {
        return weaponMode;
    }
}

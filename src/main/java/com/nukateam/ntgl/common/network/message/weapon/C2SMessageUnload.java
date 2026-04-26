package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageUnload> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            C2SMessageUnload::decode);
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private WeaponMode weaponMode;

    public C2SMessageUnload(){}

    public C2SMessageUnload(InteractionHand hand, WeaponMode weaponMode) {
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());
    }

    public static C2SMessageUnload decode(FriendlyByteBuf buffer) {
        return new C2SMessageUnload(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageUnload message, MessageContext supplier) {
        supplier.execute(() ->
            supplier.getPlayer().ifPresent((player) ->
                ServerPlayHandler.handleUnload(player, message)
            )
        );
        supplier.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getWeaponMode() {
        return weaponMode;
    }
}

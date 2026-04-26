package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class C2SMessageChangeAmmo {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageChangeAmmo> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private ResourceLocation ammo;
    private WeaponMode weaponMode;

    public C2SMessageChangeAmmo() {}

    public C2SMessageChangeAmmo(InteractionHand hand, ResourceLocation ammo, WeaponMode weaponMode) {
        this.hand = hand;
        this.ammo = ammo;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageChangeAmmo message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.ammo.toString());
        buffer.writeUtf(message.weaponMode.toString());
    }

    public static C2SMessageChangeAmmo decode(FriendlyByteBuf buffer) {
        return new C2SMessageChangeAmmo(buffer.readEnum(InteractionHand.class),
                ResourceLocation.tryParse(buffer.readUtf()),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageChangeAmmo message, MessageContext context) {
        context.execute(() ->
            context.getPlayer().ifPresent((player) ->
                ServerPlayHandler.handleAmmoChange(message, player)
            )
        );
        context.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ResourceLocation getAmmo() {
        return ammo;
    }

    public WeaponMode getWeaponMode() {
        return weaponMode;
    }
}

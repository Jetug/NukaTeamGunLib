package com.nukateam.ntgl.common.network.message;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public class C2SMessageChangeAmmo implements IMessage<C2SMessageChangeAmmo> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private ResourceLocation ammo;
    private WeaponMode weaponMode;

    public C2SMessageChangeAmmo() {}

    public C2SMessageChangeAmmo(InteractionHand hand, ResourceLocation ammo, WeaponMode weaponMode) {
        this.hand = hand;
        this.ammo = ammo;
        this.weaponMode = weaponMode;
    }

    @Override
    public void encode(C2SMessageChangeAmmo message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.ammo.toString());
        buffer.writeUtf(message.weaponMode.toString());
    }

    @Override
    public C2SMessageChangeAmmo decode(FriendlyByteBuf buffer) {
        return new C2SMessageChangeAmmo(buffer.readEnum(InteractionHand.class),
                ResourceLocation.tryParse(buffer.readUtf()),
                WeaponMode.getType(buffer.readUtf()));
    }

    @Override
    public void handle(C2SMessageChangeAmmo message, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                ServerPlayHandler.handleAmmoChange(message, player);
            }
        });
        context.setPacketHandled(true);
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

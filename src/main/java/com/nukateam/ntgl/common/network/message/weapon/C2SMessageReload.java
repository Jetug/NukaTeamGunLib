package com.nukateam.ntgl.common.network.message.weapon;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReload implements IMessage<C2SMessageReload> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    WeaponMode weaponMode;

    public C2SMessageReload() {}

    public C2SMessageReload(InteractionHand hand, WeaponMode weaponMode) {
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    @Override
    public void encode(C2SMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());
    }

    @Override
    public C2SMessageReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageReload(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    @Override
    public void handle(C2SMessageReload message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            ServerPlayer player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleReload(message, player);
            }
        }));
        supplier.setPacketHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getWeaponMode() {
        return weaponMode;
    }
}

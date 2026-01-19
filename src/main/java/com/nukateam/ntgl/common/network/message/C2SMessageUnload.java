package com.nukateam.ntgl.common.network.message;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload implements IMessage<C2SMessageUnload> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private WeaponMode weaponMode;

    public C2SMessageUnload(){}

    public C2SMessageUnload(InteractionHand hand, WeaponMode weaponMode) {
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    @Override
    public void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());
    }

    @Override
    public C2SMessageUnload decode(FriendlyByteBuf buffer) {
        return new C2SMessageUnload(buffer.readEnum(InteractionHand.class), WeaponMode.getType(buffer.readUtf()));
    }

    @Override
    public void handle(C2SMessageUnload message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> {
            var player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleUnload(player, message);
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

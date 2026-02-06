package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.enums.KeyAction;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class C2SMessageGrenade implements IMessage<C2SMessageGrenade> {
    private KeyAction action;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    WeaponMode weaponMode;

    public C2SMessageGrenade() {}

    public C2SMessageGrenade(KeyAction reload, InteractionHand hand, WeaponMode weaponMode) {
        this.action = reload;
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    @Override
    public void encode(C2SMessageGrenade message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());

    }

    @Override
    public C2SMessageGrenade decode(FriendlyByteBuf buffer) {
        return new C2SMessageGrenade(
                buffer.readEnum(KeyAction.class),
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    @Override
    public void handle(C2SMessageGrenade message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            ServerPlayer player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleGrenade(message, player);
            }
        }));
        supplier.setPacketHandled(true);
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

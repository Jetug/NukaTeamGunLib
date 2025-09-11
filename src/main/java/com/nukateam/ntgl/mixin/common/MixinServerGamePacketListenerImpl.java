package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    @Shadow public ServerPlayer player;

    @Inject(
            method = "handleSetCarriedItem(Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onHandleSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        var slot = player.getSlot(packet.getSlot());
        var item = slot.get().getItem();
        var isGun = item instanceof IWeapon;
        var isThrowable = item instanceof IThrowable;
        var hand = packet.getSlot() == Inventory.SLOT_OFFHAND ?
                InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (isGun || isThrowable) {
            var equipTime = GunStateHelper.getEquipTime(slot.get(), player);
            EquipTracker.stopEquip(player, hand);
            EquipTracker.startEquip(player, hand, equipTime);
        }
    }

}
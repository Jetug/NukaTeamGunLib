package com.nukateam.ntgl.mixin.client;

import com.nukateam.ntgl.client.util.handler.ClientEquipHandler;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    private int carriedIndex;

    @Inject(method = "ensureHasSentCarriedItem()V", at = @At(value = "HEAD"))
    private void onEnsureHasSentCarriedItem(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        int i = player.getInventory().selected;
        if (i != this.carriedIndex) {
            var hand = carriedIndex == Inventory.SLOT_OFFHAND ?
                    InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

            var item = player.getSlot(carriedIndex).get().getItem();
            var isGun = item instanceof IWeapon;
            var isThrowable = item instanceof IThrowable;

            if (isGun || isThrowable) {
                var slot = player.getSlot(carriedIndex);
                var equipTime = GunStateHelper.getEquipTime(slot.get(), player);

                ClientEquipHandler.get().setEquiping(hand, equipTime);
            }
        }
    }
}
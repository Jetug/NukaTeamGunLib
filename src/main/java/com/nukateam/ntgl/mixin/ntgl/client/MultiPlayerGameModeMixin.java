package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.client.util.handler.ClientEquipHandler;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
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

    @Inject(method = "ensureHasSentCarriedItem()V", at = @At(value = "HEAD"), remap=false)
    private void onEnsureHasSentCarriedItem(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        int selected = player.getInventory().selected;
        if (selected != this.carriedIndex) {
            var hand = carriedIndex == Inventory.SLOT_OFFHAND ?
                    InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

            var currentStack = player.getSlot(selected).get();
            var item = currentStack.getItem();
            var isGun = item instanceof IWeapon;
            var isThrowable = item instanceof IThrowable;

            if (isGun || isThrowable) {
                var equipTime = WeaponStateHelper.getEquipTime(currentStack, player);
                ClientEquipHandler.get().setEquiping(hand, equipTime);
            }
        }
    }
}
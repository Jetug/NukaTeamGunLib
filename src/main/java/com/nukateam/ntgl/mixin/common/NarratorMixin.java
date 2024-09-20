package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameNarrator.class)
public class NarratorMixin {
    @Inject(method = "isActive()Z", at = @At(value = "HEAD"), cancellable = true)
    void active(CallbackInfoReturnable<Boolean> cir) {
        var player = Minecraft.getInstance().player;
        if(player != null && player.getOffhandItem().getItem() instanceof GunItem)
            cir.setReturnValue(false);
    }
}

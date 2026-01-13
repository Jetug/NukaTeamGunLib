package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish, Jetug
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "turnPlayer()V", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(CallbackInfo ci) {
        if (ActionWheelManager.getInstance().isWheelActive()) {
            this.accumulatedDX = 0;
            this.accumulatedDY = 0;
            ci.cancel();
        }
    }

    @ModifyVariable(method = "turnPlayer()V", at = @At(value = "STORE", opcode = Opcodes.DSTORE), ordinal = 2)
    private double sensitivity(double original) {
        var additionalAdsSensitivity = 1.0F;
        var mc = Minecraft.getInstance();

        if (mc.player != null && !mc.player.getMainHandItem().isEmpty() && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            var heldItem = mc.player.getMainHandItem();
            if (heldItem.getItem() instanceof IWeapon) {
                var aimHandler = AimingHandler.get();
                if (aimHandler.isAiming() && !ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player)) {
                    float modifier = WeaponStateHelper.getFovModifier(aimHandler.getWeaponData());
                    additionalAdsSensitivity = Mth.clamp(1.0F - (1.0F / modifier) / 10F, 0.0F, 1.0F);
                }
            }
        }

//        var adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
        var adsSensitivity = NtglOptions.getInstance().getAdsSensitivity();
        return original * (1.0 - (1.0 - adsSensitivity) * AimingHandler.get().getNormalisedAdsProgress()) * additionalAdsSensitivity;
    }
}

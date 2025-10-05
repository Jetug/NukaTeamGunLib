package com.nukateam.ntgl.mixin.chassis.common;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {
    public PlayerMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    protected abstract void checkRidingStatistics(double pDistanceX, double pDistanceY, double pDistanceZ);

    @Inject(method = "wantsToStopRiding()Z", at = @At(value = "HEAD"), cancellable = true)
    private void wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
        if (isWearingChassis(this))
            cir.setReturnValue(false);
    }

    @Inject(method = "rideTick()V", at = @At(value = "HEAD"), cancellable = true)
    public void rideTick(CallbackInfo ci) {
        if(PlayerUtils.isWearingChassis(this)) {
            super.rideTick();
            ci.cancel();
        }
    }
}

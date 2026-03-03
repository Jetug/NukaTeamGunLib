package com.nukateam.ntgl.mixin.chassis.common;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {
    @Shadow private boolean reducedDebugInfo;

    public PlayerMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "wantsToStopRiding()Z", at = @At(value = "HEAD"), cancellable = true, remap=false)
    private void wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
        if (isWearingChassis(this))
            cir.setReturnValue(false);
    }

    @Inject(method = "rideTick()V", at = @At(value = "HEAD"), cancellable = true, remap=false)
    public void rideTick(CallbackInfo ci) {
        if(PlayerUtils.isWearingChassis(this)) {
            super.rideTick();
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true, remap=false)
    private float modifyDamageAmount(float amount, DamageSource source) {
        var player = this;

        if(PlayerUtils.isWearingChassis(player)){
            var chassis = PlayerUtils.getEntityChassis(player);
            assert chassis != null;
            return chassis.getDamageAfterAbsorb(source, amount);
        }
        else return amount;
    }
}

package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.foundation.entity.DamageSourceProjectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private DamageSource source;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void capture(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.source = source;
    }

    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), index = 0)
    private double modifyApplyKnockbackArgs(double original) {
        if (this.source instanceof DamageSourceProjectile) {
            if (!Config.COMMON.gameplay.enableKnockback.get()) {
                return 0;
            }

            double strength = Config.COMMON.gameplay.knockbackStrength.get();
            if (strength > 0) {
                return strength;
            }
        }
        return original;
    }

//    @Inject(method = "tick", at = @At(value = "HEAD"))
//    public void tick(CallbackInfo ci) {
//        var entity = (LivingEntity)(Object)this;
//        var heldItem = entity.getMainHandItem();
//
//        if (!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem) {
//            var model = ModelOverrides.getModel(heldItem);
//            if (model != null) {
//                model.tick(entity);
//            }
//        }
//    }
}

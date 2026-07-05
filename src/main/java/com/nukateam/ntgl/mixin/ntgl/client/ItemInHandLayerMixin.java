package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.handler.WeaponRenderingHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true, remap=false)
    private void renderArmWithItem(LivingEntity entity, ItemStack stack,
                                       ItemDisplayContext transformType, HumanoidArm arm,
                                       PoseStack poseStack, MultiBufferSource source, int light, CallbackInfo ci) {
        var hand = entity.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        if(stack != entity.getItemInHand(hand)) return;

        boolean inPA = entity.getVehicle() instanceof WearableChassis;
        if (inPA) {
            ci.cancel();
            return;
        }

        var oppositeHand = entity.getMainArm() == arm ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var oppositeStack = entity.getItemInHand(oppositeHand);

        if (hand == InteractionHand.OFF_HAND) {
            if(!WeaponModifierHelper.isOneHanded(new WeaponData(stack, entity)) || !WeaponModifierHelper.isOneHanded(new WeaponData(oppositeStack, entity))){
                ci.cancel();
                return;
            }
        }

        if (stack.getItem() instanceof IWeapon) {
            ci.cancel();
            var layer = (ItemInHandLayer<?, ?>) (Object) this;
            renderArmWithGun(layer, entity, stack, transformType, hand, arm,
                    poseStack, source, light, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
        }
    }


    //Third person render
    private static void renderArmWithGun(ItemInHandLayer<?, ?> layer, LivingEntity entity, ItemStack stack,
                                         ItemDisplayContext transformType,
                                         InteractionHand hand, HumanoidArm arm, PoseStack poseStack,
                                         MultiBufferSource source, int light, float deltaTicks) {
        poseStack.pushPose();
        {
            layer.getParentModel().translateToHand(arm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180F));
            WeaponRenderingHandler.get().applyWeaponScale(stack, poseStack);

            var gripType = WeaponModifierHelper.getGripType(new WeaponData(stack, entity));
            var aimProgress = AimingHandler.get().getAimProgress(entity, deltaTicks);
            gripType.getHeldAnimation()
                    .applyHeldItemTransforms(entity, hand, aimProgress, poseStack, source);
            WeaponRenderingHandler.get().renderWeapon(entity, stack, transformType, poseStack, source, light);
        }
        poseStack.popPose();
    }
}

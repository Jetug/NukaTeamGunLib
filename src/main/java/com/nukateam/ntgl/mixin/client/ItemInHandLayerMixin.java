package com.nukateam.ntgl.mixin.client;

import com.nukateam.ntgl.client.data.handler.AimingHandler;
import com.nukateam.ntgl.client.data.handler.GunRenderingHandler;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.nukateam.ntgl.common.data.util.GunModifierHelper.*;

/**
 * Author: MrCrayfish
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItem(LivingEntity entity, ItemStack stack,
                                       ItemTransforms.TransformType transformType, HumanoidArm arm,
                                       PoseStack poseStack, MultiBufferSource source, int light, CallbackInfo ci) {
        var hand = Minecraft.getInstance().options.mainHand == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if(stack != entity.getItemInHand(hand)) return;
        var oppositeHand = Minecraft.getInstance().options.mainHand == arm ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var oppositeStack = entity.getItemInHand(oppositeHand);

        if (hand == InteractionHand.OFF_HAND) {
            if(!canRenderInOffhand(stack) || !canRenderInOffhand(oppositeStack)){
                ci.cancel();
                return;
            }
        }

        if (stack.getItem() instanceof GunItem gunItem) {
            ci.cancel();
            var layer = (ItemInHandLayer<?, ?>) (Object) this;
            renderArmWithGun(layer, entity, stack, gunItem, transformType, hand, arm,
                    poseStack, source, light, Minecraft.getInstance().getFrameTime());
        }
    }


    //Third person render
    private static void renderArmWithGun(ItemInHandLayer<?, ?> layer, LivingEntity entity, ItemStack stack,
                                         GunItem item, ItemTransforms.TransformType transformType,
                                         InteractionHand hand, HumanoidArm arm, PoseStack poseStack,
                                         MultiBufferSource source, int light, float deltaTicks) {
        poseStack.pushPose();
        {
            layer.getParentModel().translateToHand(arm, poseStack);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            GunRenderingHandler.get().applyWeaponScale(stack, poseStack);

            var gun = item.getModifiedGun(stack);
            var heldAnimation = gun.getGeneral().getGripType().getHeldAnimation();

            heldAnimation.applyHeldItemTransforms(entity, hand, AimingHandler.get().getAimProgress(entity, deltaTicks), poseStack, source);
            GunRenderingHandler.get().renderWeapon(entity, stack, transformType, poseStack, source, light);
        }
        poseStack.popPose();
    }
}

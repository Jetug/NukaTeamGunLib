package com.nukateam.ntgl.mixin.client;

import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.handler.GunRenderingHandler;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

import static com.nukateam.ntgl.common.util.util.GunModifierHelper.*;

/**
 * Author: MrCrayfish
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItem(LivingEntity entity, ItemStack stack,
                                       ItemDisplayContext transformType, HumanoidArm arm,
                                       PoseStack poseStack, MultiBufferSource source, int light, CallbackInfo ci) {
        var minecraft = Minecraft.getInstance();
        var hand = minecraft.options.mainHand().get() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        if(stack != entity.getItemInHand(hand)) return;

        var oppositeHand = minecraft.options.mainHand().get() == arm ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var oppositeStack = entity.getItemInHand(oppositeHand);

        if (hand == InteractionHand.OFF_HAND) {
            if(!isOneHanded(new GunData(stack, entity)) || !isOneHanded(new GunData(oppositeStack, entity))){
                ci.cancel();
                return;
            }
        }

//        var reloadHandler = ClientReloadHandler.get();
//        var player = Minecraft.getInstance().player;
//
//        var isReloadingLeft = reloadHandler.isReloadingLeft(player);
//        var isRightArm = hand == InteractionHand.MAIN_HAND;
//
//        var isReloadingRight = reloadHandler.isReloadingRight(player);
//        var isLeftArm = hand == InteractionHand.OFF_HAND;
//
//        if ((isReloadingLeft && isRightArm) || (isReloadingRight && isLeftArm)) {
//            ci.cancel();
//            return;
//        }

        if (stack.getItem() instanceof WeaponItem) {
            ci.cancel();
            var layer = (ItemInHandLayer<?, ?>) (Object) this;
            renderArmWithGun(layer, entity, stack, transformType, hand, arm,
                    poseStack, source, light, minecraft.getFrameTime());
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
            GunRenderingHandler.get().applyWeaponScale(stack, poseStack);

            var heldAnimation = GunModifierHelper.getGripType(new GunData(stack, entity)).getHeldAnimation();

            heldAnimation.applyHeldItemTransforms(entity, hand, AimingHandler.get().getAimProgress(entity, deltaTicks), poseStack, source);
            GunRenderingHandler.get().renderWeapon(entity, stack, transformType, poseStack, source, light);
        }
        poseStack.popPose();
    }
}

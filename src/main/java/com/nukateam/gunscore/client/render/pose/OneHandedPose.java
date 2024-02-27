package com.nukateam.gunscore.client.render.pose;

import com.nukateam.gunscore.client.data.IHeldAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.common.base.gun.GripType;
import com.nukateam.gunscore.common.foundation.item.GunItem;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.jetug.chassis_core.client.render.renderers.CustomHandRenderer.doSafe;

/**
 * Author: MrCrayfish
 */
public class OneHandedPose implements IHeldAnimation {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
//        var right = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
        var flip = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT;

        var right = hand == InteractionHand.MAIN_HAND;
        var isOneHanded = entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof GunItem gunItem
                && gunItem.getGun().getGeneral().getGripType() != GripType.ONE_HANDED;

        if(!right && isOneHanded)
            return;

        var arm = right ? rightArm : leftArm;
        IHeldAnimation.copyModelAngles(head, arm);
        arm.xRot += (float) Math.toRadians(-70F);

        if (entity.getUseItem().getItem() == Items.SHIELD) {
            arm.xRot = (float) Math.toRadians(-30F);
        }
    }

//    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyGeoModelRotation(LivingEntity entity, AnimationProcessor animationProcessor) {
        doSafe(() -> {
            CoreGeoBone head = animationProcessor.getBone("head");
            CoreGeoBone rightArm = animationProcessor.getBone("right_arm");
            rightArm.setRotX(head.getRotX());
            rightArm.setRotY(head.getRotY());
            rightArm.setRotZ(head.getRotZ());
            rightArm.setRotX(head.getRotX() + 70);
        });
    }


    @Override
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        var side = hand == InteractionHand.OFF_HAND ? 1 : -1;
        poseStack.translate(0.45 * side, -0.5, -1.2);
    }

    @Override
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack,
                                      MultiBufferSource buffer, int light, float partialTicks) {
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
//        var model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level, player, 0);
//        var translateX = model.getTransforms().firstPersonRightHand.translation.x();
//        var translateZ = model.getTransforms().firstPersonRightHand.translation.z();
//        var side = hand == HumanoidArm.RIGHT ? -1 : 1;
//        poseStack.translate(translateX * side, 0, -translateZ);
//
//        var slim = Minecraft.getInstance().player.getModelName().equals("slim");
//        var armWidth = slim ? 3.0F : 4.0F;
//
//        poseStack.scale(0.5F, 0.5F, 0.5F);
//        poseStack.translate(0.8 * side, 1.45, -2.9);
// //       poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
////        poseStack.translate(0, 0.15, -1.3125);
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));

//        RIGHT_HAND_RENDERER.render();


//        poseStack.translate(-0.75, 0, 0);
//        RIGHT_HAND_RENDERER.renderHand(player, stack, poseStack, buffer, light);

//        RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
    }

    @Override
    public boolean applyOffhandTransforms(LivingEntity entity, HumanoidModel<LivingEntity> model, ItemStack stack, PoseStack poseStack, float partialTicks) {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));

        if (entity.isCrouching()) {
            poseStack.translate(-4.5 * 0.0625, -15 * 0.0625, -4 * 0.0625);
        } else if (!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            poseStack.translate(-4.0 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        } else {
            poseStack.translate(-3.5 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(90F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(75F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) (Math.toDegrees(model.rightLeg.xRot) / 10F)));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        return true;
    }

    @Override
    public boolean canApplySprintingAnimation() {
        return false;
    }

    @Override
    public boolean canRenderOffhandItem() {
        return true;
    }

    @Override
    public double getFallSwayZOffset() {
        return 0.5;
    }
}

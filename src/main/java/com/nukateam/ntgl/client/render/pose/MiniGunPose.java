package com.nukateam.ntgl.client.render.pose;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.base.gun.GripType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mod.azure.azurelib.core.animation.AnimationProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public class MiniGunPose extends WeaponPose {
    @Override
    protected AimPose getUpPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-100F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-150F).setRotationAngleY(40F).setRotationAngleZ(-10F).setRotationPointY(1));
        return pose;
    }

    @Override
    protected AimPose getForwardPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setRightArm(new LimbPose().setRotationAngleX(-15F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-45F).setRotationAngleY(30F).setRotationAngleZ(0F).setRotationPointY(2));
        return pose;
    }

    @Override
    protected AimPose getDownPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(-50F, 0F, 0F)).setItemTranslate(new Vector3f(0F, 0F, 1F)).setRightArm(new LimbPose().setRotationAngleX(0F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-25F).setRotationAngleY(30F).setRotationAngleZ(15F).setRotationPointY(4));
        return pose;
    }

    @Override
    protected boolean hasAimPose() {
        return false;
    }

    @Override
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        if(hand == InteractionHand.OFF_HAND) return;
        if (Config.CLIENT.display.oldAnimations.get()) {
            boolean right = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            ModelPart mainArm = right ? rightArm : leftArm;
            ModelPart secondaryArm = right ? leftArm : rightArm;
            mainArm.xRot = (float) Math.toRadians(-15F);
            mainArm.yRot = (float) Math.toRadians(-45F) * (right ? 1F : -1F);
            mainArm.zRot = (float) Math.toRadians(0F);
            secondaryArm.xRot = (float) Math.toRadians(-45F);
            secondaryArm.yRot = (float) Math.toRadians(30F) * (right ? 1F : -1F);
            secondaryArm.zRot = (float) Math.toRadians(0F);
        } else {
            super.applyHumanoidModelRotation(entity, rightArm, leftArm, head, hand, aimProgress);
        }
    }

    @Override
    public void applyGeoModelRotation(LivingEntity entity, AnimationProcessor animationProcessor) {
        var mainArm = animationProcessor.getBone("right_arm");
        var secondaryArm = animationProcessor.getBone("left_arm");

        mainArm.setRotX((float)Math.toRadians(15F));
        mainArm.setRotY((float)Math.toRadians(45F));
        mainArm.setRotZ((float)Math.toRadians(0F));
        secondaryArm.setRotX((float)Math.toRadians(45F));
        secondaryArm.setRotY((float)Math.toRadians(-30F));
        secondaryArm.setRotZ((float)Math.toRadians(0F));
    }

    public static boolean rightHandIsMain(InteractionHand hand){
        return Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT ?
                hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
    }

    @Override
    public void applyEntityPreRender(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        if (Config.CLIENT.display.oldAnimations.get()) {
            var right = rightHandIsMain(hand);
            entity.yBodyRotO = entity.yRotO + 45F * (right ? 1F : -1F);
            entity.yBodyRot = entity.getYRot() + 45F * (right ? 1F : -1F);
        } else {
            super.applyEntityPreRender(entity, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
//        if (Config.CLIENT.display.oldAnimations.get()) {
//            if (hand == InteractionHand.OFF_HAND) {
//                poseStack.translate(0, -10 * 0.0625F, 0);
//                poseStack.translate(0, 0, -2 * 0.0625F);
//            }
//        } else {
//            super.applyHeldItemTransforms(entity, hand, aimProgress, poseStack, buffer);
//        }

        // z = y; y = z; x = x
        poseStack.translate(-0.5, 0.37, -1.25);

    }

    @Override
    public boolean applyOffhandTransforms(LivingEntity entity, HumanoidModel<LivingEntity> model, ItemStack stack, PoseStack poseStack, float partialTicks) {
        return GripType.applyBackTransforms(entity, poseStack);
    }

    @Override
    public boolean canApplySprintingAnimation() {
        return false;
    }
}

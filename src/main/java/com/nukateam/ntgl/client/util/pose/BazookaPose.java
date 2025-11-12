package com.nukateam.ntgl.client.util.pose;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.holders.GripType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Author: MrCrayfish
 */
public class BazookaPose extends WeaponPose {
    @Override
    protected AimPose getUpPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(4).setRotationPointZ(-2)).setLeftArm(new LimbPose().setRotationAngleX(-130F).setRotationAngleY(65F).setRotationAngleZ(0F).setRotationPointX(3).setRotationPointY(2).setRotationPointZ(1));
        return pose;
    }

    @Override
    protected AimPose getForwardPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F)
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-90F)
                        .setRotationAngleY(-35F)
                        .setRotationAngleZ(0F)

                        .setRotationPointY(2)
                        .setRotationPointZ(0))

                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-91F)
                        .setRotationAngleY(35F)
                        .setRotationAngleZ(0F)

                        .setRotationPointX(4)
                        .setRotationPointY(2)
                        .setRotationPointZ(0));
        return pose;
    }

    @Override
    protected AimPose getDownPose() {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F).setRightArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(15F).setRotationAngleZ(30F).setRotationPointX(4).setRotationPointY(2).setRotationPointZ(0));
        return pose;
    }

    @Override
    protected boolean hasAimPose() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        if(hand == InteractionHand.OFF_HAND) return;
        if (Config.CLIENT.display.oldAnimations.get()) {
            boolean right = PlayerHelper.isRight(hand);
            ModelPart mainArm = right ? rightArm : leftArm;
            ModelPart secondaryArm = right ? leftArm : rightArm;
            mainArm.xRot = (float) Math.toRadians(-90F);
            mainArm.yRot = (float) Math.toRadians(-35F) * (right ? 1F : -1F);
            mainArm.zRot = (float) Math.toRadians(0F);
            secondaryArm.xRot = (float) Math.toRadians(-91F);
            secondaryArm.yRot = (float) Math.toRadians(45F) * (right ? 1F : -1F);
            secondaryArm.zRot = (float) Math.toRadians(0F);
        } else {
            super.applyHumanoidModelRotation(entity, rightArm, leftArm, head, hand, aimProgress);
        }
    }

    @Override
    public void applyGeoModelRotation(LivingEntity entity, CoreGeoBone rightArm, CoreGeoBone leftArm, CoreGeoBone head, InteractionHand interactionHand) {
        rightArm.setRotX((float)Math.toRadians(80F));
        rightArm.setRotY((float)Math.toRadians(35F));
        rightArm.setRotZ((float)Math.toRadians(0F));
        leftArm.setRotX((float)Math.toRadians(75));
        leftArm.setRotY((float)Math.toRadians(-30F));
        leftArm.setRotZ((float)Math.toRadians(0F));
    }

    @Override
    public void applyEntityPreRender(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        if (Config.CLIENT.display.oldAnimations.get()) {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            entity.yBodyRotO = entity.yRotO + 35F * (right ? 1F : -1F);
            entity.yBodyRot = entity.getYRot() + 35F * (right ? 1F : -1F);
        } else {
            super.applyEntityPreRender(entity, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        if (!Config.CLIENT.display.oldAnimations.get()) {
            super.applyHeldItemTransforms(entity, hand, aimProgress, poseStack, buffer);
        }
//        poseStack.translate(-0.5, -0.45, -1);
//        poseStack.translate(X * 0.0625, Y * 0.0625, Z * 0.0625);
        poseStack.translate(-10 * 0.0625, -11 * 0.0625, -20 * 0.0625);
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

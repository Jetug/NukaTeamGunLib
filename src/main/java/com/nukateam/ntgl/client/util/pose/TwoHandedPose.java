package com.nukateam.ntgl.client.util.pose;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import com.nukateam.ntgl.client.util.util.render.ModelRenderUtil;
import com.nukateam.ntgl.common.data.holders.GripType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Author: MrCrayfish
 */
public class TwoHandedPose extends WeaponPose {
    @Override
    protected AimPose getUpPose() {
        AimPose upPose = new AimPose();
        upPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(60F, 0F, 10F))
                .setRightArm(new LimbPose().setRotationAngleX(-120F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-160F).setRotationAngleY(-20F).setRotationAngleZ(-30F).setRotationPointY(2).setRotationPointZ(-1));
        upPose.getAiming()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(40F, 0F, 30F))
                .setItemTranslate(new Vector3f(-1, 0, 0))
                .setRightArm(new LimbPose().setRotationAngleX(-140F).setRotationAngleY(-55F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-20F).setRotationAngleZ(-35F).setRotationPointY(1).setRotationPointZ(0));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose() {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(30F, -11F, 0F))
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-60F).setRotationAngleY(-55F).setRotationAngleZ(0F)
                        .setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1))
                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-65F).setRotationAngleY(-10F).setRotationAngleZ(5F)
                        .setRotationPointY(2).setRotationPointZ(-1));
        forwardPose.getAiming()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(5F, -21F, 0F))
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-85F).setRotationAngleY(-65F)
                        .setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2))
                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-90F).setRotationAngleY(-15F).setRotationAngleZ(0F)
                        .setRotationPointY(2).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose() {
        var downPose = new AimPose();
        downPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(-15F, -5F, 0F))
                .setItemTranslate(new Vector3f(0, -0.5F, 0.5F))
                .setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2))
                .setLeftArm(new LimbPose().setRotationAngleX(-5F).setRotationAngleY(-20F).setRotationAngleZ(20F).setRotationPointY(5).setRotationPointZ(0));
        downPose.getAiming()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(-20F, -5F, -10F))
                .setItemTranslate(new Vector3f(0, -0.5F, 1F))
                .setRightArm(new LimbPose().setRotationAngleX(-30F).setRotationAngleY(-65F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1))
                .setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-20F).setRotationAngleZ(30F).setRotationPointY(5).setRotationPointZ(0));
        return downPose;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        if(hand == InteractionHand.OFF_HAND) return;

        if (Config.CLIENT.display.oldAnimations.get()) {
            var mc = Minecraft.getInstance();
            var right = mc.options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            var mainArm = right ? rightArm : leftArm;
            var secondaryArm = right ? leftArm : rightArm;

            mainArm.xRot = head.xRot;
            mainArm.yRot = head.yRot;
            mainArm.zRot = head.zRot;

            secondaryArm.xRot = head.xRot;
            secondaryArm.yRot = head.yRot;
            secondaryArm.zRot = head.zRot;

            mainArm.xRot = (float) Math.toRadians(-55F + aimProgress * -30F);
            mainArm.yRot = (float) Math.toRadians((-45F + aimProgress * -20F) * (right ? 1F : -1F));

            secondaryArm.xRot = (float) Math.toRadians(-42F + aimProgress * -48F);
            secondaryArm.yRot = (float) Math.toRadians((-15F + aimProgress * 5F) * (right ? 1F : -1F));
        } else {
            super.applyHumanoidModelRotation(entity, rightArm, leftArm, head, hand, aimProgress);
            float angle = this.getEntityPitch(entity);
            head.xRot = (float) Math.toRadians(angle > 0.0 ? angle * 70F : angle * 90F);
        }
    }

    @Override
    public void applyGeoModelRotation(LivingEntity entity, CoreGeoBone rightArm, CoreGeoBone leftArm, CoreGeoBone head, InteractionHand interactionHand) {
        var aimProgress = AimingHandler.get().getAimProgress(entity, Minecraft.getInstance().getFrameTime());
        var right = interactionHand == InteractionHand.MAIN_HAND;

        rightArm.setRotX((float)Math.toRadians(head.getRotX()));
        rightArm.setRotY((float)Math.toRadians(head.getRotY()));
        rightArm.setRotZ((float)Math.toRadians(head.getRotZ()));

        leftArm.setRotX((float)Math.toRadians(head.getRotX()));
        leftArm.setRotY((float)Math.toRadians(head.getRotY()));
        leftArm.setRotZ((float)Math.toRadians(head.getRotZ()));

        rightArm.setRotX((float)Math.toRadians(55F + aimProgress * 30F));
        rightArm.setRotY((float)Math.toRadians((45F + aimProgress * 20F) * (right ? 1F : -1F)));

        leftArm.setRotX((float)Math.toRadians(42F + aimProgress * 48F));
        leftArm.setRotY((float)Math.toRadians((15F + aimProgress * 5F) * (right ? 1F : -1F)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyEntityPreRender(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        if (Config.CLIENT.display.oldAnimations.get()) {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            entity.yBodyRotO = entity.yRotO + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
            entity.yBodyRot = entity.getYRot() + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
        } else {
            super.applyEntityPreRender(entity, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
//        if (Config.CLIENT.display.oldAnimations.get()) {
//            if (hand == InteractionHand.MAIN_HAND) {
//                boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ?
//                        hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
//                poseStack.translate(0, 0, 0.05);
//                float invertRealProgress = 1.0F - aimProgress;
//                poseStack.mulPose(Axis.ZP.rotationDegrees((25F * invertRealProgress) * (right ? 1F : -1F)));
//                poseStack.mulPose(Axis.YP.rotationDegrees((30F * invertRealProgress + aimProgress * -20F) * (right ? 1F : -1F)));
//                poseStack.mulPose(Axis.XP.rotationDegrees(25F * invertRealProgress + aimProgress * 5F));
//            }
//        } else {
            super.applyHeldItemTransforms(entity, hand, aimProgress, poseStack, buffer);
//        }
//        poseStack.translate(-0.41, -0.35, -1);
//        poseStack.translate(X * 0.0625 , Y * 0.0625, Z * 0.0625);
        poseStack.translate(-9 * 0.0625 , -13 * 0.0625, -25 * 0.0625);

        var aim = AimingHandler.get().getAimProgress(entity, Minecraft.getInstance().getFrameTime());
        poseStack.translate(ClientDebug.X * 0.0625 * aim, ClientDebug.Y * 0.0625 * aim, ClientDebug.Z * 0.0625 * aim);
        poseStack.translate(-1 * 0.0625 * aim , 3 * 0.0625 * aim, 0);
    }

    @Override
    public boolean applyOffhandTransforms(LivingEntity entity, HumanoidModel<LivingEntity> model, ItemStack stack, PoseStack poseStack, float partialTicks) {
        return GripType.applyBackTransforms(entity, poseStack);
    }
}

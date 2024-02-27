package com.nukateam.gunscore.client.render.pose;

import com.nukateam.gunscore.client.data.IHeldAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * A simple class that handles interpolating between different poses depending on the rotation pitch
 * of the player. Used for pointing the weapon in the same direction the playing is looking.
 * <p>
 * Author: MrCrayfish
 */
public abstract class WeaponPose implements IHeldAnimation {
    private AimPose upPose;
    private AimPose forwardPose;
    private AimPose downPose;

    public WeaponPose() {
        this.upPose = this.getUpPose();
        this.forwardPose = this.getForwardPose();
        this.downPose = this.getDownPose();
    }

    /**
     * Gets the pose of the player when looking directly up
     */
    protected abstract AimPose getUpPose();

    /**
     * Gets the pose of the player when looking directly forward
     */
    protected abstract AimPose getForwardPose();

    /**
     * Gets the pose of the player when looking directly down
     */
    protected abstract AimPose getDownPose();

    /**
     * If this weapon pose has an aim pose
     */
    protected boolean hasAimPose() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm,
                                           ModelPart head, InteractionHand hand, float aimProgress) {
        var mc = Minecraft.getInstance();
        var right = mc.options.mainHand == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
        var mainArm = right ? rightArm : leftArm;
        var secondaryArm = right ? leftArm : rightArm;

        float angle = this.getEntityPitch(entity);
        float angleAbs = Math.abs(angle);
        float zoom = this.hasAimPose() ? aimProgress : 0F;
        AimPose targetPose = angle > 0.0 ? this.downPose : this.upPose;
        this.applyAimPose(targetPose, mainArm, secondaryArm, angleAbs, zoom, right ? 1 : -1, entity.isCrouching());
    }

    @Override
    public void applyGeoModelRotation(LivingEntity entity, AnimationProcessor animationProcessor) {
//        var mc = Minecraft.getInstance();
//
//        var rightArm = animationProcessor.getBone("right_arm");
//        var leftArm = animationProcessor.getBone("left_arm");
//
//        float angle = this.getEntityPitch(entity);
//        float angleAbs = Math.abs(angle);
//        float zoom = this.hasAimPose() ? AimingHandler.get().getAimProgress(entity, mc.getFrameTime()) : 0F;
//        var targetPose = angle > 0.0 ? this.downPose : this.upPose;
//        this.applyAimPose(targetPose, rightArm, leftArm, angleAbs, zoom, 1, entity.isCrouching());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyEntityPreRender(LivingEntity entity, InteractionHand hand, float aimProgress,
                                     PoseStack poseStack, MultiBufferSource buffer) {
        boolean right = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT ?
                hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
        float angle = this.getEntityPitch(entity);
        float angleAbs = Math.abs(angle);
        float zoom = this.hasAimPose() ? aimProgress : 0F;
        var targetPose = angle > 0.0 ? this.downPose : this.upPose;
        float rightOffset = this.getValue(targetPose.getIdle().getRenderYawOffset(),
                targetPose.getAiming().getRenderYawOffset(), this.forwardPose.getIdle().getRenderYawOffset(),
                this.forwardPose.getAiming().getRenderYawOffset(), 0F, angleAbs, zoom, right ? 1 : -1);
        entity.yBodyRotO = entity.yRotO + rightOffset;
        entity.yBodyRot = entity.getYRot() + rightOffset;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress,
                                        PoseStack poseStack, MultiBufferSource buffer) {

//        var side = hand == InteractionHand.OFF_HAND ? 1 : -1;
//        poseStack.translate((side * 3) / 16F, 0, -0.625);
//
//        if (hand == InteractionHand.MAIN_HAND) {
//            var right = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT;
//            var leftHanded = right ? 1 : -1;
//            poseStack.translate(0, 0, 0.05);
//
//            var angle = this.getEntityPitch(entity);
//            var angleAbs = Math.abs(angle);
//            var zoom = this.hasAimPose() ? aimProgress : 0F;
//            var targetPose = angle > 0.0 ? this.downPose : this.upPose;
//
//            var idle = targetPose.getIdle();
//            var aiming = targetPose.getAiming();
//            var translateX = this.getValue(idle.getItemTranslate().x(), aiming.getItemTranslate().x(), this.forwardPose.getIdle().getItemTranslate().x(), this.forwardPose.getAiming().getItemTranslate().x(), 0F, angleAbs, zoom, 1F);
//            var translateY = this.getValue(idle.getItemTranslate().y(), aiming.getItemTranslate().y(), this.forwardPose.getIdle().getItemTranslate().y(), this.forwardPose.getAiming().getItemTranslate().y(), 0F, angleAbs, zoom, 1F);
//            var translateZ = this.getValue(idle.getItemTranslate().z(), aiming.getItemTranslate().z(), this.forwardPose.getIdle().getItemTranslate().z(), this.forwardPose.getAiming().getItemTranslate().z(), 0F, angleAbs, zoom, 1F);
//            poseStack.translate(translateX * 0.0625 * leftHanded, translateY * 0.0625, translateZ * 0.0625);
//
//            var rotateX = this.getValue(idle.getItemRotation().x(), aiming.getItemRotation().x(), this.forwardPose.getIdle().getItemRotation().x(), this.forwardPose.getAiming().getItemRotation().x(), 0F, angleAbs, zoom, 1F);
//            var rotateY = this.getValue(idle.getItemRotation().y(), aiming.getItemRotation().y(), this.forwardPose.getIdle().getItemRotation().y(), this.forwardPose.getAiming().getItemRotation().y(), 0F, angleAbs, zoom, 1F);
//            var rotateZ = this.getValue(idle.getItemRotation().z(), aiming.getItemRotation().z(), this.forwardPose.getIdle().getItemRotation().z(), this.forwardPose.getAiming().getItemRotation().z(), 0F, angleAbs, zoom, 1F);
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(rotateX));
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotateY * leftHanded));
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotateZ * leftHanded));
//        }
    }

    /**
     * Gets the pitch of the player with an except when a screen is open. If a screen is open, the
     * pitch will always be zero.
     *
     * @param entity the player the pose is being applied to
     * @return the current pitch of the player
     */
    protected float getEntityPitch(LivingEntity entity) {
        if (Minecraft.getInstance().getCameraEntity() == entity && Minecraft.getInstance().screen != null) {
            return 0F;
        }
        return Mth.lerp(Minecraft.getInstance().getFrameTime(), entity.xRotO, entity.getXRot()) / 90F;
    }

    private void applyAimPose(AimPose targetPose, CoreGeoBone rightArm, CoreGeoBone leftArm,
                              float partial, float zoom, float offhand, boolean sneaking) {
        this.applyLimbPoseToModelRenderer(
                targetPose.getIdle().getRightArm(),
                targetPose.getAiming().getRightArm(),
                this.forwardPose.getIdle().getRightArm(),
                this.forwardPose.getAiming().getRightArm(),
                rightArm, partial, zoom, offhand, sneaking);
        this.applyLimbPoseToModelRenderer(
                targetPose.getIdle().getLeftArm(),
                targetPose.getAiming().getLeftArm(),
                this.forwardPose.getIdle().getLeftArm(),
                this.forwardPose.getAiming().getLeftArm(),
                leftArm, partial, zoom, offhand, sneaking);
    }

    private void applyLimbPoseToModelRenderer(LimbPose targetIdlePose, LimbPose targetAimingPose,
                                              LimbPose idlePose, LimbPose aimingPose, CoreGeoBone modelPart,
                                              float partial, float zoom, float leftHanded, boolean sneaking) {
        modelPart.setRotX(getValue(targetIdlePose.getRotationAngleX(), targetAimingPose.getRotationAngleX(), idlePose.getRotationAngleX(), aimingPose.getRotationAngleX(), modelPart.getRotX(), partial, zoom, 1F));
        modelPart.setRotY(getValue(targetIdlePose.getRotationAngleY(), targetAimingPose.getRotationAngleY(), idlePose.getRotationAngleY(), aimingPose.getRotationAngleY(), modelPart.getRotY(), partial, zoom, leftHanded));
        modelPart.setRotZ(getValue(targetIdlePose.getRotationAngleZ(), targetAimingPose.getRotationAngleZ(), idlePose.getRotationAngleZ(), aimingPose.getRotationAngleZ(), modelPart.getRotZ(), partial, zoom, leftHanded));
//        modelPart.setPosX(getValue(targetIdlePose.getRotationPointX(), targetAimingPose.getRotationPointX(), idlePose.getRotationPointX(), aimingPose.getRotationPointX(), modelPart.getRotX(), partial, zoom, leftHanded));
//        modelPart.setPosY(getValue(targetIdlePose.getRotationPointY(), targetAimingPose.getRotationPointY(), idlePose.getRotationPointY(), aimingPose.getRotationPointY(), modelPart.getRotY(), partial, zoom, 1F) + (sneaking ? 2F : 0F));
//        modelPart.setPosZ(getValue(targetIdlePose.getRotationPointZ(), targetAimingPose.getRotationPointZ(), idlePose.getRotationPointZ(), aimingPose.getRotationPointZ(), modelPart.getRotZ(), partial, zoom, 1F));
    }

    private void applyAimPose(AimPose targetPose, ModelPart rightArm, ModelPart leftArm,
                              float partial, float zoom, float offhand, boolean sneaking) {
        this.applyLimbPoseToModelRenderer(
                targetPose.getIdle().getRightArm(),
                targetPose.getAiming().getRightArm(),
                this.forwardPose.getIdle().getRightArm(),
                this.forwardPose.getAiming().getRightArm(),
                rightArm, partial, zoom, offhand, sneaking);
        this.applyLimbPoseToModelRenderer(
                targetPose.getIdle().getLeftArm(),
                targetPose.getAiming().getLeftArm(),
                this.forwardPose.getIdle().getLeftArm(),
                this.forwardPose.getAiming().getLeftArm(),
                leftArm, partial, zoom, offhand, sneaking);
    }

    private void applyLimbPoseToModelRenderer(LimbPose targetIdlePose, LimbPose targetAimingPose,
                                              LimbPose idlePose, LimbPose aimingPose, ModelPart modelPart,
                                              float partial, float zoom, float leftHanded, boolean sneaking) {
        modelPart.xRot = (float) Math.toRadians(getValue(targetIdlePose.getRotationAngleX(), targetAimingPose.getRotationAngleX(), idlePose.getRotationAngleX(), aimingPose.getRotationAngleX(), modelPart.xRot, partial, zoom, 1F));
        modelPart.yRot = (float) Math.toRadians(getValue(targetIdlePose.getRotationAngleY(), targetAimingPose.getRotationAngleY(), idlePose.getRotationAngleY(), aimingPose.getRotationAngleY(), modelPart.yRot, partial, zoom, leftHanded));
        modelPart.zRot = (float) Math.toRadians(getValue(targetIdlePose.getRotationAngleZ(), targetAimingPose.getRotationAngleZ(), idlePose.getRotationAngleZ(), aimingPose.getRotationAngleZ(), modelPart.zRot, partial, zoom, leftHanded));
        modelPart.x = getValue(targetIdlePose.getRotationPointX(), targetAimingPose.getRotationPointX(), idlePose.getRotationPointX(), aimingPose.getRotationPointX(), modelPart.x, partial, zoom, leftHanded);
        modelPart.y = getValue(targetIdlePose.getRotationPointY(), targetAimingPose.getRotationPointY(), idlePose.getRotationPointY(), aimingPose.getRotationPointY(), modelPart.y, partial, zoom, 1F) + (sneaking ? 2F : 0F);
        modelPart.z = getValue(targetIdlePose.getRotationPointZ(), targetAimingPose.getRotationPointZ(), idlePose.getRotationPointZ(), aimingPose.getRotationPointZ(), modelPart.z, partial, zoom, 1F);
    }



    private float getValue(@Nullable Float t1, @Nullable Float t2, Float s1, Float s2, Float def, float partial, float zoom, float leftHanded) {
        float start = t1 != null && s1 != null ? (s1 + (t1 - s1) * partial) * leftHanded : (s1 != null ? s1 * leftHanded : def);
        float end = t2 != null && s2 != null ? (s2 + (t2 - s2) * partial) * leftHanded : (s2 != null ? s2 * leftHanded : def);
        return Mth.lerp(zoom, start, end);
    }
}

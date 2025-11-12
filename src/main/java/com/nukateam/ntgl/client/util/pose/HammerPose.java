package com.nukateam.ntgl.client.util.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.ClientDebug;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class HammerPose extends WeaponPose {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        super.applyHumanoidModelRotation(entity, rightArm, leftArm, head, hand, aimProgress);

        var right = hand == InteractionHand.MAIN_HAND;

        if(!right) return;

//        IHeldAnimation.copyModelAngles(head, rightArm);
//        IHeldAnimation.copyModelAngles(head, leftArm);
//        arm.xRot += (float) Math.toRadians(-70F);
//        rightArm.xRot += (float) -60;
//        rightArm.yRot += (float) 5;
//        rightArm.zRot += (float) -25;

//        rightArm.xRot += (float) Math.toRadians(-65);
//        rightArm.yRot += (float) Math.toRadians(-15);
//        rightArm.zRot += (float) Math.toRadians(-35);
//
//        leftArm.xRot += (float) Math.toRadians(-65);
//        leftArm.yRot += (float) Math.toRadians(35 );
//        leftArm.zRot += (float) Math.toRadians(-10);

        leftArm.xRot += (float) Math.toRadians(ClientDebug.X);
        leftArm.yRot += (float) Math.toRadians(ClientDebug.Y);
        leftArm.zRot += (float) Math.toRadians(ClientDebug.Z);

        if (entity.getUseItem().getItem() == Items.SHIELD) {
            rightArm.xRot = (float) Math.toRadians(-30F);
        }
    }

    @Override
    protected AimPose getUpPose() {
        var upPose = new AimPose();
        upPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(60F, 0F, 10F))
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-120f + 5)
                        .setRotationAngleY(-55F - 5)
                        .setRotationAngleZ(-10f - 40)
                        .setRotationPointX(-5 )
                        .setRotationPointY(3  )
                        .setRotationPointZ(0) )
                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-160F + 20 + 10)
                        .setRotationAngleY(-20F - 10 + 5)
                        .setRotationAngleZ(-30F - 30)
                        .setRotationPointY(2)
                        .setRotationPointZ(-1));
         return upPose;
    }

    @Override
    protected AimPose getForwardPose() {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(30F, -11F, 0F))
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-65)
                        .setRotationAngleY(-15)
                        .setRotationAngleZ(-35)

                        .setRotationPointX(-5)
                        .setRotationPointY(2)
                        .setRotationPointZ(1))
                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-65)
                        .setRotationAngleY(55)
                        .setRotationAngleZ(-25)

                        .setRotationPointY(2)
                        .setRotationPointZ(-1));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose() {
        var downPose = new AimPose();
        downPose.getIdle()
                .setRenderYawOffset(45F)
                .setItemRotation(new Vector3f(-15F, -5F, 0F))
                .setItemTranslate(new Vector3f(0, -0.5F, 0.5F))
                .setRightArm(new LimbPose()
                        .setRotationAngleX(-30F - 10)
                        .setRotationAngleY(-65F + 60)
                        .setRotationAngleZ(0F -40)

                        .setRotationPointX(-5)
                        .setRotationPointY(2))
                .setLeftArm(new LimbPose()
                        .setRotationAngleX(-5F)
                        .setRotationAngleY(-20F + 50)
                        .setRotationAngleZ(20F + 10)

                        .setRotationPointY(5)
                        .setRotationPointZ(0));
        return downPose;
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void applyEntityPreRender(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
//        boolean right = !PlayerHelper.isRight(hand);
//        entity.yBodyRotO = entity.yRotO + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
//        entity.yBodyRot = entity.getYRot() + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
//    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyGeoModelRotation(LivingEntity entity, CoreGeoBone rightArm, CoreGeoBone leftArm, CoreGeoBone head, InteractionHand interactionHand) {
        try {
            var right = interactionHand == InteractionHand.MAIN_HAND;
            var arm = right ? rightArm : leftArm;

            arm.setRotX(head.getRotX());
            arm.setRotY(head.getRotY());
            arm.setRotZ(head.getRotZ());
            arm.setRotX(head.getRotX() + 70);
        }
        catch (Exception e){
            Ntgl.LOGGER.debug(e.getMessage(), e);
        }

    }


    @Override
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
        var side = hand == InteractionHand.OFF_HAND ? 1 : -1;
        poseStack.translate(0.45 * side, -0.5, -1.2);

        var right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
        var leftHanded = right ? 1 : -1;

//        poseStack.mulPose(Axis.XP.rotationDegrees(ClientDebug.X));
//        poseStack.mulPose(Axis.YP.rotationDegrees(ClientDebug.Y * leftHanded));
//        poseStack.mulPose(Axis.ZP.rotationDegrees(ClientDebug.Z * leftHanded));

//        poseStack.mulPose(Axis.ZP.rotationDegrees(-30 * leftHanded));
//
        poseStack.translate(-20 / 10d * 0.0625, -30 / 10d * 0.0625, -10 / 10d * 0.0625);
//        poseStack.translate(-55 / 10d * 0.0625, 10 / 10d * 0.0625, 15 / 10d * 0.0625);


    }

    @Override
    public boolean applyOffhandTransforms(LivingEntity entity, HumanoidModel<LivingEntity> model, ItemStack stack, PoseStack poseStack, float partialTicks) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));

        if (entity.isCrouching()) {
            poseStack.translate(-4.5 * 0.0625, -15 * 0.0625, -4 * 0.0625);
        } else if (!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            poseStack.translate(-4.0 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        } else {
            poseStack.translate(-3.5 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(90F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(75F));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (Math.toDegrees(model.rightLeg.xRot) / 10F)));
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

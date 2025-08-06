package com.nukateam.ntgl.client.util.pose;

import com.jetug.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.IHeldAnimation;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.GripType;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HammerPose implements IHeldAnimation {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
        var right = hand == InteractionHand.MAIN_HAND;

        if(!right) return;

        IHeldAnimation.copyModelAngles(head, rightArm);
        IHeldAnimation.copyModelAngles(head, leftArm);
//        arm.xRot += (float) Math.toRadians(-70F);
//        rightArm.xRot += (float) -60;
//        rightArm.yRot += (float) 5;
//        rightArm.zRot += (float) -25;

        rightArm.xRot += (float) Math.toRadians(-65);
        rightArm.yRot += (float) Math.toRadians(-15);
        rightArm.zRot += (float) Math.toRadians(-35);

        leftArm.xRot += (float) Math.toRadians(-65);
        leftArm.yRot += (float) Math.toRadians(35);
        leftArm.zRot += (float) Math.toRadians(-10);

        leftArm.xRot += (float) Math.toRadians(ClientDebug.X);
        leftArm.yRot += (float) Math.toRadians(ClientDebug.Y);
        leftArm.zRot += (float) Math.toRadians(ClientDebug.Z);

        if (entity.getUseItem().getItem() == Items.SHIELD) {
            rightArm.xRot = (float) Math.toRadians(-30F);
        }
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
////        poseStack.translate(ClientDebug.X / 10d * 0.0625, ClientDebug.Y / 10d * 0.0625, ClientDebug.Z / 10d * 0.0625);
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

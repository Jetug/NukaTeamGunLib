package com.nukateam.ntgl.client.util.pose;

import com.jetug.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.IHeldAnimation;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import com.nukateam.ntgl.client.util.util.render.ModelRenderUtil;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.GripType;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Author: MrCrayfish
 */
public class HammerPose implements IHeldAnimation {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHumanoidModelRotation(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress) {
//        var right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;

        var flip = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
        var stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
        var right = hand == InteractionHand.MAIN_HAND;
        var isNotOneHanded = stack.getItem() instanceof WeaponItem
                && !GunModifierHelper.getGripType(new GunData(stack, entity)).isOneHanded();

        if(!right && isNotOneHanded)
            return;

        var arm = right ? rightArm : leftArm;
        IHeldAnimation.copyModelAngles(head, arm);
        arm.xRot += (float) Math.toRadians(-70F);

        if (entity.getUseItem().getItem() == Items.SHIELD) {
            arm.xRot = (float) Math.toRadians(-30F);
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
        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
        entity.yBodyRotO = entity.yRotO + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);
        entity.yBodyRot = entity.getYRot() + (right ? 25F : -25F) + aimProgress * (right ? 20F : -20F);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(LivingEntity entity, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer) {
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

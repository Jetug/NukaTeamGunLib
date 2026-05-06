package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.helpers.TransformUtils;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.nukateam.ntgl.client.render.GeoRenderUtils.*;
import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class ArmedModelRenderer<Animator extends ItemAnimator> extends DynamicGeoItemRenderer<Animator> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_ARM_ANIM = "right_arm_anim";
    public static final String LEFT_ARM_ANIM = "left_arm_anim";
    protected MultiBufferSource bufferSource;
    protected boolean firstRightRender = true;
    protected boolean firstLeftRender = true;
    private ItemDisplayContext transformType;

    public ArmedModelRenderer(GeoModel<Animator> model) {
        super(model);
        addRenderLayer(new GlowingLayer<>(this));
        ClientTickHandler.addTicker(this, this::tick);
    }

    protected void tick(){}

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.firstRightRender = true;
        this.firstLeftRender  = true;
        this.currentEntity = entity;

        poseStack.pushPose();
        {
            poseStack.translate(0, 0, -50 / 10d / 16d);
            super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  int colour) {
        poseStack.pushPose();

        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms(poseStack, bone, packedLight, packedOverlay, bufferSource);
            }
            case LEFT_ARM_ANIM, RIGHT_ARM_ANIM ->{
                if(!TransformUtils.isFirstPerson(transformType)){
                    bone.setHidden(true);
                }
                else bone.setHidden(false);
            }
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, colour);
        poseStack.popPose();
    }


    protected void renderArms(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay, MultiBufferSource bufferSource) {
        var client = Minecraft.getInstance();
        if(client.player == null) return;

        var isRightHand = this.transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var isLeftHand = this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (bone.getName().equals(RIGHT_ARM)){
            if(!firstRightRender)
                return;
            firstRightRender = false;
        }
        if (bone.getName().equals(LEFT_ARM)){
            if(!firstLeftRender)
                return;
            firstLeftRender = false;
        }

        if (isRightHand || isLeftHand) {
            poseStack.pushPose();
            {
                RenderUtil.prepMatrixForBone(poseStack, bone);
                poseStack.translate(0.01, -0.27, 0.05);
                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

                if(ChassisHelper.isPlayerInChassis()){
                    if(isRightHand) {
                        if (bone.getName().equals(LEFT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.LEFT, packedLight);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.RIGHT, packedLight);
                        }
                    } else {
                        if (bone.getName().equals(LEFT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.RIGHT, packedLight);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.LEFT, packedLight);
                        }
                    }
                }
                else {
                    if (isRightHand) {
                        if (bone.getName().equals(LEFT_ARM)) {
//                            poseStack.translate(-8 / 10d / 16d, 0, 0);
//                            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
//                            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
                            poseStack.translate(-65 / 10d / 16d, 0 / 10d / 16d, 0 / 10d / 16d);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, false);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
//                            poseStack.translate(4 / 10d / 16d, 0, -3 / 10d / 16d);
                            poseStack.translate(50 / 10d / 16d, -20 / 10d / 16d, 0 / 10d / 16d);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, true);
                        }
                    } else {
                        if (bone.getName().equals(LEFT_ARM)) {
                            poseStack.translate(50 / 10d / 16d, -20 / 10d / 16d, 0 / 10d / 16d);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, true);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            poseStack.translate(-65 / 10d / 16d, 0 / 10d / 16d, 0 / 10d / 16d);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, false);
                        }
                    }
                }
            }
            poseStack.popPose();
        }
    }
}

package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderLeftArm;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderRightArm;

public class ThrowableItemRenderer<Animator extends ItemAnimator> extends DynamicGeoItemRenderer<Animator> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    protected MultiBufferSource bufferSource;
    protected ItemStack stack;
    protected boolean firstRightRender = true;
    protected boolean firstLeftRender = true;
    private ItemDisplayContext transformType;

    public ThrowableItemRenderer(GeoModel<Animator> model) {
        super(model);
        addRenderLayer(new GlowingLayer<>(this));
        ClientTickHandler.addTicker(this, this::tick);
    }

    protected void tick(TickEvent event){
        if (event.phase == TickEvent.Phase.START){

        }
    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.stack = stack;
        this.firstRightRender = true;
        this.firstLeftRender  = true;
        this.currentEntity = entity;

        poseStack.pushPose();
        {
            var isLeftHand = this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            if(isLeftHand){
                poseStack.scale(1 + ClientDebug.X, 1 + ClientDebug.Y, 1 + ClientDebug.Z);
            }
            if(TransformUtils.isFirstPerson(transformType)){
                poseStack.translate(0, -6 / 16D, 0);
            }
            else if (TransformUtils.isThirdPerson(transformType)){
                poseStack.translate(0, -9 / 16D, 0);
            }
            else if(TransformUtils.isNonHand(transformType)){
                poseStack.translate(0, -8 / 16D, 0);
            }
            else {
                poseStack.translate(0, -8 / 16D, 0);
                poseStack.translate(0, ClientDebug.Y / 10d / 16D, 0);
            }
            super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        poseStack.pushPose();

        var name = bone.getName();

        if (name.equals(LEFT_ARM) || name.equals(RIGHT_ARM)) {
            bone.setHidden(true);
            bone.setChildrenHidden(false);
            renderArms(poseStack, animatable, bone, renderType, bufferSource,
                    isReRender, partialTick, packedLight, packedOverlay, new Rgba(red, green, blue, alpha));
        }

        renderRecursivelyPost(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    public LivingEntity getRenderEntity() {
        return currentEntity;
    }

    protected void renderRecursivelyPost(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                         MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                         float partialTick, int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha) {}

    protected void renderArms(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                              MultiBufferSource bufferSource, boolean isReRender, float partialTick,
                              int packedLight, int packedOverlay, Rgba rgba) {
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
                RenderUtils.prepMatrixForBone(poseStack, bone);
                poseStack.translate(0.01, -0.27, 0.05);
                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());


                if(ChassisHelper.isPlayerInChassis()){
                    if (isRightHand) {
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
                    var playerSkin = ((LocalPlayer) ClientUtils.getClientPlayer()).getSkinTextureLocation();
                    var arm = this.bufferSource.getBuffer(RenderType.entitySolid(playerSkin));
                    var sleeve = this.bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));

                    if (isRightHand) {
                        if (bone.getName().equals(LEFT_ARM)) {
                            renderLeftArm(poseStack, bone, packedLight, packedOverlay, arm, sleeve);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            renderRightArm(poseStack, bone, packedLight, packedOverlay, arm, sleeve);
                        }
                    } else {
                        if (bone.getName().equals(LEFT_ARM)) {
                            renderRightArm(poseStack, bone, packedLight, packedOverlay, arm, sleeve);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            renderLeftArm(poseStack, bone, packedLight, packedOverlay, arm, sleeve);
                        }
                    }
                }
            }
            poseStack.popPose();
        }
    }
}

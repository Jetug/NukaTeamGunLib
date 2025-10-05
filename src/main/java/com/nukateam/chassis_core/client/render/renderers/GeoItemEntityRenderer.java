package com.nukateam.chassis_core.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;
import mod.azure.azurelib.renderer.GeoObjectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;

public class GeoItemEntityRenderer<T extends GeoAnimatable> extends GeoObjectRenderer<T> {
    private ItemStack currentItemStack;
    private ItemDisplayContext currentTransform;

    public GeoItemEntityRenderer(GeoModel<T> model) {
        super(model);
    }

    public void render(ItemStack stack, ItemDisplayContext transformType,
                       PoseStack poseStack, T animatable,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType,
                       @Nullable VertexConsumer buffer,
                       int packedLight) {
        this.currentItemStack = stack;
        this.currentTransform = transformType;
        super.render(poseStack, animatable, bufferSource, renderType, buffer, packedLight);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        poseStack.pushPose();

        boolean shouldSit = false;
        float lerpBodyRot = 0;
        float lerpHeadRot = 0;
        float netHeadYaw = lerpHeadRot - lerpBodyRot;
        float limbSwingAmount;
        limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!isReRender) {
            var headPitch = 0;
            var motionThreshold = 0;
            var velocity = Vec3.ZERO;//nukateam
            var avgVelocity = (float) (Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
            var animationState = new AnimationState(animatable, limbSwing, limbSwingAmount, partialTick,
                    avgVelocity >= motionThreshold && limbSwingAmount != 0.0F);
            var instanceId = this.getInstanceId(animatable);

            animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
            animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
//            animatable.getAnimatableInstanceCache().getManagerForId(instanceId).setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
            animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
            animationState.setData(DataTickets.ENTITY, null);//jet
            animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, false, -netHeadYaw, -headPitch));
            var var31 = this.model;
            Objects.requireNonNull(animationState);
            var31.addAdditionalStateData(animatable, instanceId, animationState::setData);
            this.model.handleAnimations(animatable, instanceId, animationState);
        }

        poseStack.translate(0.0, 0.009999999776482582, 0.0);
        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }
}
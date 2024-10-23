package com.nukateam.example.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;

public class SimpleEntityRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {
    private float scale = 1;

    public SimpleEntityRenderer(EntityRendererProvider.Context renderManager) {
        this(renderManager, new EntityModel<>());
    }

    public SimpleEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        {
            scale(poseStack, getScale());
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        {
            scaleBone(bone, 1.0f);

            if (bone.getName().contains("head") && animatable.isBaby()) {
                scaleBone(bone, 1.5f);
            }

            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                    isReRender, partialTick,
                    packedLight, packedOverlay, red, green, blue, alpha);
        }
        poseStack.popPose();
    }

    private static void scaleBone(GeoBone bone, float value) {
        bone.setScaleX(value);
        bone.setScaleY(value);
        bone.setScaleZ(value);
    }

    private static void scale(PoseStack poseStack, float scale) {
        poseStack.scale(scale, scale, scale);
    }

    public float getScale() {
        return scale;
    }

    public SimpleEntityRenderer<T> setScale(float scale) {
        this.scale = scale;
        return this;
    }
}
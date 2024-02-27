package com.nukateam.gunscore.client.render.renderers;

import com.nukateam.gunscore.client.animators.ItemAnimator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;
import mod.azure.azurelib.renderer.GeoObjectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
public class GeoDynamicItemRenderer<T extends ItemAnimator> extends GeoObjectRenderer<T> {
    private final Map<LivingEntity, Map<TransformType, T>> animatorsByTransform = new HashMap<>();
    private final Function<TransformType, T> animatorFactory;
    protected ItemStack currentItemStack;
    protected TransformType currentTransform;
    protected LivingEntity currentEntity;

    public GeoDynamicItemRenderer(GeoModel<T> model, Function<TransformType, T> animatorFactory) {
        super(model);
        this.animatorFactory = animatorFactory;
    }

    public void render(LivingEntity entity, ItemStack stack, TransformType transformType,
                       PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType,
                       @Nullable VertexConsumer buffer,
                       int packedLight) {
        this.currentItemStack = stack;
        this.currentTransform = transformType;
        this.currentEntity = entity;
        super.render(poseStack, getRenderItem(entity, transformType), bufferSource, renderType, buffer, packedLight);
    }

    public T getRenderItem(LivingEntity entity, TransformType transformType) {
        if (!animatorsByTransform.containsKey(entity)) {
            animatorsByTransform.put(entity, createAnimators());
        }
        return animatorsByTransform.get(entity).get(transformType);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        {
            boolean shouldSit = false;
            float lerpBodyRot = 0;
            float lerpHeadRot = 0;
            float netHeadYaw = lerpHeadRot - lerpBodyRot;
            float limbSwingAmount;
            limbSwingAmount = 0.0F;
            float limbSwing = 0.0F;

            if (!isReRender)
                setupRender(animatable, isReRender, partialTick, shouldSit, netHeadYaw, limbSwingAmount, limbSwing);

            poseStack.translate(0.0, 0.009999999776482582, 0.0);
            this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
        poseStack.popPose();
    }

    protected Map<TransformType, T> createAnimators(){
        var result = new HashMap<TransformType, T>();
        for (var transform: TransformType.values())
            result.put(transform, animatorFactory.apply(transform));
        return result;
    }

    private void setupRender(T animatable, boolean isReRender, float partialTick, boolean shouldSit, float netHeadYaw, float limbSwingAmount, float limbSwing) {
        var headPitch = 0;
        var motionThreshold = 0;
        var velocity = Vec3.ZERO;//jetug
        var avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
        var animationState = new AnimationState(animatable, limbSwing, limbSwingAmount, partialTick,
                avgVelocity >= motionThreshold && limbSwingAmount != 0.0F);
        var instanceId = this.getInstanceId(animatable);

        animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
        animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
//            animatable.getAnimatableInstanceCache().getManagerForId(instanceId).setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
        animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
        animationState.setData(DataTickets.ENTITY, currentEntity);
        animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, false, -netHeadYaw, -headPitch));
        var var31 = this.model;
        Objects.requireNonNull(animationState);
        var31.addAdditionalStateData(animatable, instanceId, animationState::setData);
        this.model.handleAnimations(animatable, instanceId, animationState);
    }
}

package com.nukateam.geo.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.registry.WeaponRegistry;
import net.minecraft.client.Minecraft;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class DynamicGeoItemRenderer<Animator extends ItemAnimator> extends GeoObjectRenderer<Animator> {
    private final Map<Pair<LivingEntity, ItemDisplayContext>, Animator> animatorsByTransform = new HashMap<>();
    private BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<Animator>, Animator> animatorFactory = null;
    private ItemStack currentStack;
    private ItemDisplayContext currentTransform;
    protected LivingEntity currentEntity;
    private LivingEntity buffEntity = null;

    public DynamicGeoItemRenderer(GeoModel<Animator> model) {
        super(model);
    }

    public DynamicGeoItemRenderer(GeoModel<Animator> model, BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<Animator>, Animator> animatorFactory) {
        super(model);
        this.animatorFactory = animatorFactory;
    }

    @Override
    public void defaultRender(PoseStack poseStack, Animator animatable,
                              MultiBufferSource bufferSource, @Nullable RenderType renderType,
                              @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        animatable.setStack(currentStack);
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }


    @Override
    public void actuallyRender(PoseStack poseStack, Animator animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               int colour) {
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

            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        }
        poseStack.popPose();
    }

    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType,
                       PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType,
                       @Nullable VertexConsumer buffer,
                       int packedLight) {
        this.currentStack = stack;
        this.currentTransform = transformType;
        this.currentEntity = entity;

        if(buffEntity != null){
            currentEntity = buffEntity;
            buffEntity = null;
        }

        var partialTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        super.render(poseStack, getAnimator(currentEntity, transformType, stack), bufferSource, renderType, buffer, packedLight, partialTick);
    }

    public Animator getAnimator(LivingEntity entity, ItemDisplayContext transformType, ItemStack stack) {
        var key = Pair.of(entity, transformType);
        if (!animatorsByTransform.containsKey(key)) {
            if(animatorFactory == null) {
                animatorFactory = WeaponRegistry.getAnimator(stack.getItem());
            }
            animatorsByTransform.put(key, animatorFactory.apply(transformType, this));
        }

        return animatorsByTransform.get(key);
    }

    public LivingEntity getRenderEntity() {
        return currentEntity;
    }

    public void setEntity(LivingEntity entity) {
        this.buffEntity = entity;
    }

    private void setupRender(Animator animatable, boolean isReRender, float partialTick, boolean shouldSit, float netHeadYaw, float limbSwingAmount, float limbSwing) {
        var headPitch = 0;
        var motionThreshold = 0;
        var velocity = Vec3.ZERO;//nukateam
        var avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
        var animationState = new AnimationState(animatable, limbSwing, limbSwingAmount, partialTick,
                avgVelocity >= motionThreshold && limbSwingAmount != 0.0F);
        var instanceId = this.getInstanceId(animatable);

        animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
        animationState.setData(DataTickets.ITEMSTACK, this.currentStack);
//        animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
        animationState.setData(DataTickets.ENTITY, currentEntity);
        animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, false, -netHeadYaw, -headPitch));
        var var31 = this.model;
        Objects.requireNonNull(animationState);
        var31.addAdditionalStateData(animatable, instanceId, animationState::setData);
        this.model.handleAnimations(animatable, instanceId, animationState, partialTick);
    }
}

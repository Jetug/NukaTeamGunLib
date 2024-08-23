package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.animators.ItemAnimator;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

public class DynamicGunRenderer<T extends ItemAnimator> extends GeoDynamicItemRenderer<T> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    private ItemDisplayContext transformType;
    private MultiBufferSource bufferSource;
    private ItemStack renderStack;
    protected LivingEntity buffEntity = null;

    public DynamicGunRenderer(GeoModel<T> model, BiFunction<ItemDisplayContext, GeoDynamicItemRenderer<T>, T> animatorFactory) {
        super(model, animatorFactory);
        addRenderLayer(new GlowingLayer<>(this));
    }

    public ItemStack getRenderStack() {
        return renderStack;
    }

    public LivingEntity getRenderEntity() {
        return currentEntity;
    }

    public void setEntity(LivingEntity entity) {
        this.buffEntity = entity;
    }

//    @Override
//    public float getMotionAnimThreshold(GunAnimator animatable) {
//        return 0.005f;
//    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.renderStack = stack;

        if(buffEntity != null){
            entity = buffEntity;
            buffEntity = null;
        }

        if(entity instanceof Raider){
            Ntgl.LOGGER.debug("");
        }

        super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//        if(bone.getName().equals(RIGHT_ARM) || bone.getName().equals(LEFT_ARM)){
//            bone.setHidden(!renderHands);
//        }
        var client = Minecraft.getInstance();
        var renderArms = false;

        //hiding the arm bones so they can get redone below
        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms = true;
            }
        }

        renderAttachments(renderStack, bone);

        //after hiding the bones and checking of your display type to render them in, in this case first and third person
        var isRightHand = this.transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var isLeftHand = this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (renderArms && isRightHand || isLeftHand) {
            var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
            var playerEntityModel = playerEntityRenderer.getModel();
            poseStack.pushPose();
            {
                RenderUtils.prepMatrixForBone(poseStack, bone);
//                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
//                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
                poseStack.translate(0.01, -0.27, 0.05);
//                poseStack.translate(X, Y, Z);

//                poseStack.mulPose(Axis.XP.rotationDegrees(180));

                assert (client.player != null);

                var playerSkin = ((LocalPlayer) ClientUtils.getClientPlayer()).getSkinTextureLocation();
                var arm = this.bufferSource.getBuffer(RenderType.entitySolid(playerSkin));
                var sleeve = this.bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));

                if(isRightHand) {
                    if (bone.getName().equals(LEFT_ARM)) {
                        renderLeftArm(poseStack, bone, packedLight, packedOverlay, playerEntityModel, arm, sleeve);
                    } else if (bone.getName().equals(RIGHT_ARM)) {
                        renderRightArm(poseStack, bone, packedLight, packedOverlay, playerEntityModel, arm, sleeve);
                    }
                }
                else{
                    if (bone.getName().equals(LEFT_ARM)) {
                        renderRightArm(poseStack, bone, packedLight, packedOverlay, playerEntityModel, arm, sleeve);
                    } else if (bone.getName().equals(RIGHT_ARM)) {
                        renderLeftArm(poseStack, bone, packedLight, packedOverlay, playerEntityModel, arm, sleeve);
                    }
                }
            }
            poseStack.popPose();
        }
        // This super call is needed with the custom getBuffer call for the weapon model to get it's texture back and not use the players skin
        super.renderRecursively(poseStack, animatable, bone, renderType,
                bufferSource, this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, red, green, blue, alpha);

//        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static void renderRightArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay, PlayerModel<AbstractClientPlayer> playerEntityModel, VertexConsumer arm, VertexConsumer sleeve) {
        playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightArm.setRotation(0, 0, 0);
        playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

        playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightSleeve.setRotation(0, 0, 0);
        playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
    }

    private static void renderLeftArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay, PlayerModel<AbstractClientPlayer> playerEntityModel, VertexConsumer arm, VertexConsumer sleeve) {
        playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftArm.setRotation(0, 0, 0);
        playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

        playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftSleeve.setRotation(0, 0, 0);
        playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
    }

    protected void renderAttachments(ItemStack stack, GeoBone bone) {
        var gun = ((GunItem)stack.getItem()).getModifiedGun(stack);
        var configAttachments = gun.getModules().getAttachments();

        if(configAttachments != null) {
            var attachment = gun.getModules().getAttachmentByBone(bone.getName());

            if(attachment != null){
                var gunAttachments = Gun.getAttachments(stack);
                for (var att: gunAttachments) {
                    var registryName = ForgeRegistries.ITEMS.getKey(att.getItem());
                    if(registryName.equals(attachment.getItem()))
                        return;
                }
                bone.setHidden(true);
            }
        }
//        var config = item.getConfig();
//
//        if (config != null) {
//            var allMods = config.mods;
////            var visibleMods = StackUtils.getAttachments(stack);
//            var names = getAttachmentNames(stack);
//
//            for (var name : allMods)
//                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(true));
////            for (var name : visibleMods)
////                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(false));
//            for (var name : names)
//                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(false));
//        }
    }
}

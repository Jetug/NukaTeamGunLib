package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.animators.ItemAnimator;
import com.nukateam.ntgl.client.data.util.TransformUtils;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.common.base.gun.AttachmentType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.data.util.Rgba;
import com.nukateam.ntgl.common.foundation.item.BarrelItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiFunction;

import static com.nukateam.ntgl.client.event.InputEvents.*;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderLeftArm;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderRightArm;

public class DynamicGunRenderer<T extends ItemAnimator> extends GeoDynamicItemRenderer<T> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String MUZZLE_FLASH = "muzzle_flash";
    private ItemDisplayContext transformType;
    private MultiBufferSource bufferSource;
    private ItemStack renderStack;
    protected LivingEntity buffEntity = null;
    protected ArrayList<ItemStack> gunAttachments;
    protected ArrayList<Gun.Modules.Attachment> configAttachments;
    protected ArrayList<String> hiddenBones = new ArrayList<>();
    protected BarrelItem barrelItem;
    protected Gun gun;

    public DynamicGunRenderer(GeoModel<T> model, BiFunction<ItemDisplayContext, GeoDynamicItemRenderer<T>, T> animatorFactory) {
        super(model, animatorFactory);
        addRenderLayer(new GlowingLayer<>(this));
    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.renderStack = stack;
        this.gun = GunModifierHelper.getGun(stack);
        this.gunAttachments = Gun.getAttachmentItems(stack);
        this.configAttachments = gun.getAttachments(gunAttachments);
        var barrelStack = Gun.getAttachmentItem(AttachmentType.BARREL, renderStack);
        hiddenBones.clear();

        if(barrelStack.getItem() instanceof BarrelItem barrelItem)
            this.barrelItem = barrelItem;
        else this.barrelItem = null;

        for (var attachment : configAttachments) {
            hiddenBones.addAll(attachment.getHidden());
        }

        if(buffEntity != null){
            entity = buffEntity;
            buffEntity = null;
        }

        super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        //hiding the arm bones so they can get redone below

        renderAttachments(bone);

        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                var rgba = new Rgba(red, green, blue, alpha);
                renderArms(poseStack, animatable, bone, renderType, bufferSource,
                        isReRender, partialTick, packedLight, packedOverlay, rgba);
                return;
            }
            case MUZZLE_FLASH -> {
                if(barrelItem != null){
                    var length = barrelItem.getProperties().getLength();
                    poseStack.pushPose();
                    {
                        poseStack.translate(0, 0, -length / 16D);
                        if (Ntgl.isDebugging())
                            poseStack.translate(X / 16D, Y / 16D, Z / 16D);
                        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                                buffer, isReRender, partialTick, packedLight, packedOverlay,
                                red, green, blue, alpha);
                    }
                    poseStack.popPose();
                    return;
                }
            }
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);

    }

    protected void renderArms(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                           MultiBufferSource bufferSource, boolean isReRender, float partialTick,
                           int packedLight, int packedOverlay, Rgba rgba) {
        var client = Minecraft.getInstance();
        if(client.player == null) return;

        var isRightHand = this.transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var isLeftHand = this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (isRightHand || isLeftHand) {
            var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
            var playerEntityModel = playerEntityRenderer.getModel();
            poseStack.pushPose();
            {
                RenderUtils.prepMatrixForBone(poseStack, bone);
                poseStack.translate(0.01, -0.27, 0.05);

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
                packedOverlay, rgba.r(), rgba.g(), rgba.b(), rgba.a());
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

    protected void renderAttachments(GeoBone bone) {
        var boneName = bone.getName();
        if(hiddenBones.stream().anyMatch((s) -> s.equals(boneName))) {
            bone.setHidden(true);
            return;
        }

        var configAttachments = gun.getModules().getAttachments();
        if (configAttachments == null) {
            bone.setHidden(false);
            return;
        }

        var attachment = gun.getModules().getAttachmentByBone(boneName);
        if (attachment != null) {
            if(transformType == ItemDisplayContext.GUI){
                bone.setHidden(true);
                return;
            }

            for (var att : this.gunAttachments) {
                var registryName = ForgeRegistries.ITEMS.getKey(att.getItem());
                if (registryName != null && registryName.equals(attachment.getItem())) {
                    bone.setHidden(false);
                    return;
                }
            }
            bone.setHidden(true);
            return;
        }
        
        bone.setHidden(false);
    }
}

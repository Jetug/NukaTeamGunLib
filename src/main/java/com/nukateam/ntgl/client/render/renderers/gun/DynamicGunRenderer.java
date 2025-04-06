package com.nukateam.ntgl.client.render.renderers.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.event.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.common.data.config.gun.Modules;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.foundation.item.attachment.BarrelItem;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.event.InputEvents.*;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderLeftArm;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderRightArm;

public class DynamicGunRenderer<Animator extends ItemAnimator> extends DynamicGeoItemRenderer<Animator> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String MUZZLE_FLASH = "muzzle_flash";
    protected MultiBufferSource bufferSource;
    protected ArrayList<ItemStack> gunAttachments;
    protected ArrayList<Modules.Attachment> configAttachments;
    protected ArrayList<String> hiddenBones = new ArrayList<>();
    protected BarrelItem barrelItem;
    protected Gun gun;
    protected ItemStack gunStack;
    protected boolean firstRightRender = true;
    protected boolean firstLeftRender = true;
    private ItemDisplayContext transformType;

    public DynamicGunRenderer(GeoModel<Animator> model) {
        super(model);
        addRenderLayer(new GlowingLayer<>(this));
        ClientTickHandler.addClientTicker(this, this::tick);
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
        this.gun = GunModifierHelper.getGun(stack);
        this.gunStack = stack;
        this.gunAttachments = Gun.getAttachmentItems(stack);
        this.configAttachments = gun.getAttachments(gunAttachments);
        this.firstRightRender = true;
        this.firstLeftRender  = true;
        this.currentEntity = entity;

        if (TransformUtils.isFirstPerson(transformType) && AimingHandler.isScoping(stack))
            return;

        var barrelStack = Gun.getAttachmentItem(AttachmentType.BARREL, stack);

        if(barrelStack.getItem() instanceof BarrelItem barrel) {
            this.barrelItem = barrel;
        }
        else this.barrelItem = null;

        prepareHiddenBones(transformType);

        poseStack.pushPose();
        {
            poseStack.translate(0, /*InputEvents.Y / 16D*/ -6 / 16D, 0);
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
        renderAttachments(bone);

        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms(poseStack, animatable, bone, renderType, bufferSource,
                        isReRender, partialTick, packedLight, packedOverlay, new Rgba(red, green, blue, alpha));
            }
            case MUZZLE_FLASH -> {
                if(barrelItem != null){
                    renderMuzzleFlash(poseStack);
                }
            }
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

    protected boolean shouldRenderAttachment(Modules.Attachment attachment, ItemStack item) {
        if (transformType != ItemDisplayContext.GUI) {
            var itemId = ForgeRegistries.ITEMS.getKey(item.getItem());
            return !item.isEmpty() && attachment.getItemId().equals(itemId);
        }
        return false;
    }

    protected void renderAttachments(GeoBone bone) {
        var boneName = bone.getName();
        var hideBone = hiddenBones.stream().anyMatch((s) -> s.equals(boneName));
        bone.setHidden(hideBone);
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
            var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
            var playerEntityModel = playerEntityRenderer.getModel();
            poseStack.pushPose();
            {
                RenderUtils.prepMatrixForBone(poseStack, bone);
                poseStack.translate(0.01, -0.27, 0.05);
                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

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
    }

    protected void renderMuzzleFlash(PoseStack poseStack) {
        var length = barrelItem.getProperties().getLength();
        poseStack.translate(0, 0, -length / 16D);
        if (Ntgl.isDebugging())
            poseStack.translate(-(double) X / 10 / 16D, (double) Y / 10 / 16D, (double) Z / 10 / 16D);
    }

    protected void prepareHiddenBones(ItemDisplayContext transformType) {
        if(gunStack == null || gunStack.isEmpty()) return;

        var gunAttachments = this.gun.getModules().getAttachments();
        hiddenBones.clear();
        gunAttachments.forEach((type, typeAttachments) -> {
            var item = Gun.getAttachmentItem(type, gunStack);

            typeAttachments.forEach((attachment) -> {
                if(shouldRenderAttachment(attachment, item)){
                    if(transformType != ItemDisplayContext.GUI) {
                        hiddenBones.addAll(attachment.getHidden());
                    }
                }
                else {
                    hiddenBones.add(attachment.getName());
                    hiddenBones.addAll(attachment.getBones());
                }
            });
        });
    }
}

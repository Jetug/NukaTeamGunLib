package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.common.data.config.gun.Modules;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
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
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderLeftArm;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderRightArm;
import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class DynamicGunRenderer<Animator extends ItemAnimator> extends ArmsRenderer<Animator> {
    public static final String MUZZLE_FLASH = "muzzle_flash";
    protected MultiBufferSource bufferSource;
    protected ArrayList<ItemStack> gunAttachments;
    protected ArrayList<Modules.Attachment> configAttachments;
    protected ArrayList<String> hiddenBones = new ArrayList<>();
    protected BarrelItem barrelItem;
    protected Gun gun;
    protected ItemStack gunStack;
    private ItemDisplayContext transformType;

    public DynamicGunRenderer(GeoModel<Animator> model) {
        super(model);
        addRenderLayer(new GlowingLayer<>(this));
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
            if(TransformUtils.isNonHand(transformType)){
                poseStack.translate(0, -7.5D/* ClientDebug.Y / 10D / 16D*/, 0);
            }
            else poseStack.translate(0, -6 / 16D, 0);
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

        if (bone.getName().equals(MUZZLE_FLASH)) {
            if (barrelItem != null) {
                renderMuzzleFlash(poseStack);
            }
        }

        renderRecursivelyPost(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, new Rgba(red, green, blue, alpha));

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }
    
    protected void renderRecursivelyPost(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                         MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                         float partialTick, int packedLight, int packedOverlay, Rgba rgba) {}

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

    protected void renderMuzzleFlash(PoseStack poseStack) {
        var length = barrelItem.getProperties().getLength();
        poseStack.translate(0, 0, -length / 16D);
        if (Ntgl.isDebugging())
            poseStack.translate(-X / 10D / 16D, Y / 10D / 16D, Z / 10D / 16D);
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

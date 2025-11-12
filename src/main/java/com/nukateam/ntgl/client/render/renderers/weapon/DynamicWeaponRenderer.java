package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.client.util.helpers.TransformUtils;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.Modules;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.foundation.item.attachment.BarrelItem;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class DynamicWeaponRenderer<Animator extends ItemAnimator> extends ArmedModelRenderer<Animator> {
    public static final String MUZZLE_FLASH = "muzzle_flash";
    protected MultiBufferSource bufferSource;
    protected ArrayList<ItemStack> gunAttachments;
    protected ArrayList<Modules.Attachment> configAttachments;
    protected ArrayList<String> hiddenBones = new ArrayList<>();
    protected BarrelItem barrelItem;
    protected WeaponConfig weaponConfig;
    protected ItemStack gunStack;
    private ItemDisplayContext transformType;

    public DynamicWeaponRenderer(GeoModel<Animator> model) {
        super(model);
    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.weaponConfig = WeaponModifierHelper.getConfig(new WeaponData(stack, entity));
        this.gunStack = stack;
        this.gunAttachments = WeaponStateHelper.getAttachmentItems(stack);
        this.configAttachments = weaponConfig.getAttachmentConfigs(gunAttachments);
        this.currentEntity = entity;

        if (TransformUtils.isFirstPerson(transformType) && AimingHandler.isScoping(stack))
            return;

        var barrelStack = WeaponStateHelper.getAttachmentItem(AttachmentType.BARREL, stack);

        if(barrelStack.getItem() instanceof BarrelItem barrel) {
            this.barrelItem = barrel;
        }
        else this.barrelItem = null;

        prepareHiddenBones(transformType);

        poseStack.pushPose();
        {
            if(TransformUtils.isNonHand(transformType)){
                poseStack.translate(0, -7.5D / 16D/* ClientDebug.Y / 10D / 16D*/, 0);
            }
            else if(TransformUtils.isFirstPerson(transformType)){
//                poseStack.translate(X / 10d / 16d, -25 / 10d / 16d, 5 / 10d / 16d);
                poseStack.translate(0, -8.5 / 16D, 0.5 / 16D);
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
                                  int colour) {
        poseStack.pushPose();
        renderAttachments(bone);

        if (bone.getName().equals(MUZZLE_FLASH)) {
            if (barrelItem != null) {
                renderMuzzleFlash(poseStack);
            }
        }

        renderRecursivelyPost(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, colour);
        poseStack.popPose();
    }
    
    protected void renderRecursivelyPost(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                         MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                         float partialTick, int packedLight, int packedOverlay, int colour) {}


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

        hiddenBones.clear();
        var gunAttachments = this.weaponConfig.getModules().getAttachments();

        var visibleBones = new ArrayList<String>();

        gunAttachments.forEach((type, typeAttachments) -> {
            var item = WeaponStateHelper.getAttachmentItem(type, gunStack);

            for (var attachment : typeAttachments) {
                if (shouldRenderAttachment(attachment, item)) {
                    if (transformType != ItemDisplayContext.GUI) {
                        hiddenBones.addAll(attachment.getHidden());
                        visibleBones.add(attachment.getName());
                        visibleBones.addAll(attachment.getBones());
                    }
                } else {
                    hiddenBones.add(attachment.getName());
                    hiddenBones.addAll(attachment.getBones());
                }
            }
        });

        hiddenBones.removeAll(visibleBones);
    }

    protected boolean shouldRenderAttachment(Modules.Attachment attachment, ItemStack item) {
        if (transformType != ItemDisplayContext.GUI) {
            var itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
            return !item.isEmpty() && attachment.getItemId().equals(itemId);
        }
        return false;
    }
}

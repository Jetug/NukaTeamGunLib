package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
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
import software.bernie.geckolib.util.RenderUtil;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class DynamicWeaponRenderer<Animator extends ItemAnimator> extends ArmedModelRenderer<Animator> {
    public static final String MUZZLE_FLASH = "muzzle_flash";
    public static final String MUZZLE_FLAME = "flame";

    protected MultiBufferSource bufferSource;
    protected ArrayList<ItemStack> gunAttachments;
    protected ArrayList<Modules.Attachment> configAttachments;
    protected ArrayList<String> hiddenBones = new ArrayList<>();
    protected BarrelItem barrelItem;
    protected WeaponConfig weaponConfig;
    protected ItemStack gunStack;
    private ItemDisplayContext transformType;
    private Matrix4f bestMuzzleMatrix;
    private int bestMuzzleScore = -1;

    private static final int MUZZLE_GEOMETRY_SCORE = 4;
    private static final int MUZZLE_VISIBLE_SCORE = 2;
    private static final int MUZZLE_CANONICAL_SCORE = 1;

    public DynamicWeaponRenderer(GeoModel<Animator> model) {
        super(model);
    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.bestMuzzleMatrix = null;
        this.bestMuzzleScore = -1;
        var data = new WeaponData(stack, entity);
        this.weaponConfig = WeaponModifierHelper.getConfig(data);
        this.gunStack = stack;
        this.gunAttachments = WeaponStateHelper.getAttachmentItems(stack, entity.level().registryAccess());
        this.configAttachments = weaponConfig.getAttachmentConfigs(gunAttachments);
        this.currentEntity = entity;

        if (TransformUtils.isFirstPerson(transformType) && AimingHandler.isScoping(data))
            return;

        var barrelStack = WeaponStateHelper.getAttachmentItem(AttachmentType.BARREL, data);

        if(barrelStack.getItem() instanceof BarrelItem barrel) {
            this.barrelItem = barrel;
        }
        else this.barrelItem = null;

        prepareHiddenBones(transformType);

        poseStack.pushPose();
        {
            var offset = WeaponModifierHelper.getWeaponOffset(new WeaponData(stack, entity));
            poseStack.translate(offset.x / 16D, offset.y / 16D, offset.z / 16D);
            poseStack.translate(weaponX / 10d / 16D , weaponY / 10d / 16D, weaponZ / 10d / 16D);

            if(TransformUtils.isFirstPerson(transformType)){
                poseStack.translate(1.5 / 16D, -1.0 / 16D, 0.0 / 16D);
            }
            else if(TransformUtils.isThirdPerson(transformType)){
                poseStack.translate(0.0 / 16D, 3.5 / 16D, 2.5 / 16D);
            }
            else {
                var staticOffset = WeaponModifierHelper.getNonHandOffset(new WeaponData(stack, entity));
                poseStack.translate(staticOffset.x / 16D, staticOffset.y / 16D, staticOffset.z / 16D);
                poseStack.translate(0, -0.5 / 16D, 0 / 16d);
            }
            super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
        }
        poseStack.popPose();

        recordBestMuzzle();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  int colour) {
        poseStack.pushPose();
        renderAttachments(bone);

        if (bone.getName().equals(MUZZLE_FLASH) && barrelItem != null) {
            renderMuzzleFlash(poseStack);
        }

        if (bone.getName().startsWith(MUZZLE_FLASH) && !TransformUtils.isHandTransform(transformType)) {
            bone.setHidden(true);
        }

        if (TransformUtils.isHandTransform(transformType) && isMuzzleBone(bone)) {
            considerMuzzleCandidate(poseStack, bone);
        }

        renderRecursivelyPost(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, colour);
        poseStack.popPose();
    }

    protected static boolean isMuzzleBone(GeoBone bone) {
        return isCanonicalMuzzleName(bone.getName()) || bone.getName().startsWith(MUZZLE_FLASH);
    }

    private static boolean isCanonicalMuzzleName(String name) {
        return name.equals(MUZZLE_FLASH) || name.equals(MUZZLE_FLAME);
    }

    private void considerMuzzleCandidate(PoseStack poseStack, GeoBone bone) {
        var centroid = getFlashCentroid(bone);
        var score = (centroid == null ? 0 : MUZZLE_GEOMETRY_SCORE)
                + (isParentChainHidden(bone) ? 0 : MUZZLE_VISIBLE_SCORE)
                + (isCanonicalMuzzleName(bone.getName()) ? MUZZLE_CANONICAL_SCORE : 0);

        if (bestMuzzleMatrix != null && score <= bestMuzzleScore) return;

        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        var mat = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();

        if (centroid != null) {
            var muzzle = new Vector3f(centroid);
            mat.transformPosition(muzzle);
            mat.m30(muzzle.x).m31(muzzle.y).m32(muzzle.z);
        } else {
            mat.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
        }

        bestMuzzleScore = score;
        bestMuzzleMatrix = mat;
    }

    private static boolean isParentChainHidden(GeoBone bone) {
        for (var parent = bone.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.isHidden()) return true;
        }
        return false;
    }

    private void recordBestMuzzle() {
        if (bestMuzzleMatrix == null) return;

        var isFirstPerson = TransformUtils.isFirstPerson(transformType);
        var isThirdPerson = TransformUtils.isThirdPerson(transformType);
        if (!isFirstPerson && !isThirdPerson) return;

        var entity = this.getRenderEntity();
        if (entity == null) return;

        MuzzleMatrixHelper.saveMuzzleMatrix(entity.getId(), bestMuzzleMatrix, isFirstPerson);
        if (isFirstPerson) {
            MuzzleMatrixHelper.lastMuzzleMatrix = bestMuzzleMatrix;
        } else {
            MuzzleMatrixHelper.lastThirdPersonMuzzleMatrix = bestMuzzleMatrix;
        }
    }

    @Nullable
    private static Vector3f getFlashCentroid(GeoBone bone) {
        var cubes = bone.getCubes();
        if (cubes.isEmpty()) return null;

        var sum = new Vector3f();
        var count = 0;
        for (var cube : cubes) {
            for (var quad : cube.quads()) {
                if (quad == null) continue;
                for (var vertex : quad.vertices()) {
                    if (vertex == null) continue;
                    sum.add(vertex.position());
                    count++;
                }
            }
        }
        return count == 0 ? null : sum.div(count);
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
            poseStack.translate(-muzzleFlashX / 10D / 16D, muzzleFlashY / 10D / 16D, muzzleFlashZ / 10D / 16D);
    }

    protected void prepareHiddenBones(ItemDisplayContext transformType) {
        if(gunStack == null || gunStack.isEmpty()) return;

        hiddenBones.clear();
        var gunAttachments = this.weaponConfig.getModules().getAttachments();

        var visibleBones = new ArrayList<String>();
        var data = new WeaponData(gunStack, currentEntity);

        gunAttachments.forEach((type, typeAttachments) -> {
            var item = WeaponStateHelper.getAttachmentItem(type, data);

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

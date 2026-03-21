package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.render.renderers.weapon.DynamicWeaponRenderer;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nukateam.geo.render.ItemAnimator;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;

@Mixin(DynamicWeaponRenderer.class)
public class DynamicWeaponRendererMixin {

    @Shadow
    private ItemDisplayContext transformType;

    @Inject(method = "renderRecursively", at = @At(value = "INVOKE", target = "Lcom/nukateam/ntgl/client/render/renderers/weapon/DynamicWeaponRenderer;renderRecursivelyPost(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/nukateam/geo/render/ItemAnimator;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIILcom/nukateam/ntgl/common/util/data/Rgba;)V", remap = false), remap = false)
    private void ntgl$extractMuzzleMatrix(PoseStack poseStack, ItemAnimator animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (!bone.isHidden() && bone.getName().startsWith(DynamicWeaponRenderer.MUZZLE_FLASH)) {
            Matrix4f mat = new Matrix4f(poseStack.last().pose());
            mat.translate(
                (bone.getPivotX() + bone.getPosX()) / 16.0f,
                (bone.getPivotY() + bone.getPosY()) / 16.0f,
                (bone.getPivotZ() + bone.getPosZ()) / 16.0f
            );

            boolean isFirstPerson = this.transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                    || this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            boolean isThirdPerson = this.transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                    || this.transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

            LivingEntity entity = ((DynamicWeaponRenderer<?>) (Object) this).getRenderEntity();

            if (entity != null) {
                if (isFirstPerson) {
                    MuzzleMatrixHelper.saveMuzzleMatrix(entity.getId(), mat, true);
                    MuzzleMatrixHelper.lastMuzzleMatrix = mat;
                } else if (isThirdPerson) {
                    MuzzleMatrixHelper.saveMuzzleMatrix(entity.getId(), mat, false);
                    MuzzleMatrixHelper.lastThirdPersonMuzzleMatrix = mat;
                }
            }
        }
    }
}

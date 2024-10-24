package com.nukateam.ntgl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.geo.DynamicGeoItem;
import com.nukateam.ntgl.client.render.Render;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At(value = "HEAD"))
    public void renderStatic(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pTransformType,
                             boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, Level pLevel,
                             int pCombinedLight, int pCombinedOverlay, int pSeed, CallbackInfo ci) {
        if(pItemStack.getItem() instanceof DynamicGeoItem gunItem){
            gunItem.getRenderer().setEntity(pEntity);
        }
    }
}

package com.nukateam.ntgl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.client.util.handler.GunRenderingHandler;
import com.nukateam.ntgl.common.base.utils.DeathType;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(GeoEntityRenderer.class)
public class GeoEntityRendererMixin<T extends Entity & GeoAnimatable> {
    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "HEAD"), cancellable = true)
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        var dt = ClientProxy.getDamageType(entity);

        if (dt == DeathType.LASER || dt == DeathType.FIRE || dt == DeathType.GORE) {
            ci.cancel();
        }
    }
}

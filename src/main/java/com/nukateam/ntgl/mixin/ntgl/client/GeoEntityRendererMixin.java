package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.data.enums.DeathType;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

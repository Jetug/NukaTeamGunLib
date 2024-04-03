package com.nukateam.ntgl.client.render.entity;

import com.nukateam.ntgl.common.foundation.entity.FlameProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FlameRenderer extends EntityRenderer<FlameProjectile> {
    public FlameRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FlameProjectile entity) {
        return null;
    }

//    @Override
//    public void render(ProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
//
//    }
}

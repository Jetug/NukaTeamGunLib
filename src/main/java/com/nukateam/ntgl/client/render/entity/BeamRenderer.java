package com.nukateam.ntgl.client.render.entity;

import com.nukateam.ntgl.common.foundation.entity.TeslaProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BeamRenderer extends EntityRenderer<TeslaProjectile> {
    protected BeamRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(TeslaProjectile pEntity) {
        return null;
    }
}

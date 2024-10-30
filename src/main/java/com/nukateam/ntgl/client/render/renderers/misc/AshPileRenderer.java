package com.nukateam.ntgl.client.render.renderers.misc;

import com.nukateam.ntgl.client.model.misc.AshPileModel;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AshPileRenderer extends GeoEntityRenderer<AshPile> {
    public AshPileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AshPileModel());
    }
}
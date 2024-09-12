
package com.nukateam.example.client;

import com.nukateam.example.common.Raider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class RaiderRenderer extends HumanoidMobRenderer<Raider, RaiderModel> {
    public RaiderRenderer(EntityRendererProvider.Context context) {
        super(context, new RaiderModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new RaiderModel(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new RaiderModel(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelManager()));
    }
    @Override
    public ResourceLocation getTextureLocation(Raider entity) {
        return entity.getTexture();
    }
}

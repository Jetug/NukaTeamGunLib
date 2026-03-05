package com.nukateam.example.client;
import com.nukateam.example.common.entities.ExampleGeoEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@OnlyIn(Dist.CLIENT)
public class ExampleGeoRenderer extends GeoEntityRenderer<ExampleGeoEntity> {
    public ExampleGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ExampleGeoModel());
    }
}
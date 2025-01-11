package com.nukateam.ntgl.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.model.IGlowingModel;
import com.nukateam.ntgl.common.util.util.ResourceUtils;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static mod.azure.azurelib.cache.texture.GeoAbstractTexture.appendToPath;

public class GlowingLayer<T extends GeoAnimatable> extends LayerBase<T> {
    public static HashMap<ResourceLocation, Boolean> textures = new HashMap<>();
    public GlowingLayer(GeoRenderer<T> entityRenderer) {
        super(entityRenderer);
    }

    protected boolean resourceExists(ResourceLocation location){
        if (!textures.containsKey(location))
            textures.put(location, ResourceUtils.resourceExists(location));

        return textures.get(location);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var model = (IGlowingModel<T>)getRenderer().getGeoModel();
        var texture = model.getGlowingTextureResource(animatable);
        if(resourceExists(texture))
            renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);
    }
}
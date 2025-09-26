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

import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class GlowingLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    public static HashMap<ResourceLocation, Boolean> textures = new HashMap<>();
    public GlowingLayer(GeoRenderer<T> entityRenderer) {
        super(entityRenderer);
    }

    protected boolean resourceExists(ResourceLocation location){
        return textures.computeIfAbsent(location, ResourceUtils::resourceExists);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var model = (IGlowingModel<T>)getRenderer().getGeoModel();
        var texture = model.getGlowingTextureResource(animatable);

        if(resourceExists(texture)) {
            renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);
        }
    }

    protected void renderLayer(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, MultiBufferSource bufferSource, float partialTick, int packedLight, ResourceLocation texture) {
        var renderTypeNew = RenderType.entityTranslucentEmissive(texture);

        poseStack.pushPose();
        {
            poseStack.translate(-0.5, -0.5, -0.5);
            poseStack.translate(0, -16 / 100d / 16D, 0);
            poseStack.translate( X / 100d / 16d, Y / 100d / 16d, Z / 10d / 16d);
            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                    renderTypeNew, bufferSource.getBuffer(renderTypeNew),
                    partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();
    }
}
package com.nukateam.chassis_core.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerBase<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    public LayerBase(GeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    protected void renderLayer(PoseStack poseStack, T entity, BakedGeoModel bakedModel, MultiBufferSource bufferSource, float partialTick, int packedLight, ResourceLocation texture) {
        int overlay = OverlayTexture.NO_OVERLAY;
        var renderTypeNew = RenderType.armorCutoutNoCull(texture);
        poseStack.pushPose();
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.translate(0.0d, 0.0d, 0.0d);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, entity,
                renderTypeNew, bufferSource.getBuffer(renderTypeNew), partialTick,
                packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}

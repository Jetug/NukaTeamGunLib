package com.nukateam.ntgl.client.render.layers;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.GunMod;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.ntgl.client.data.util.TextureUtils.getTextureSize;

public class LocalPlayerSkinLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private Pair<Integer, Integer> size;

    public LocalPlayerSkinLayer(GeoRenderer<T> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(size == null) this.size = getTextureSize(getRenderer().getTextureLocation(animatable));
        poseStack.translate(-0.5, -0.5, -0.5);
        var player = Minecraft.getInstance().player;
        if(player == null) return;

        var texture = GunMod.SKIN_STORAGE.getSkin(player, size);

        if (texture != null) {
            renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);
        }
    }

    protected void renderLayer(PoseStack poseStack, T entity, BakedGeoModel bakedModel, MultiBufferSource bufferSource, float partialTick, int packedLight, ResourceLocation texture) {
        int overlay = OverlayTexture.NO_OVERLAY;
        RenderType renderTypeNew = RenderType.armorCutoutNoCull(texture);
        poseStack.pushPose();
        poseStack.scale(1.0F, 1.0F, 1.0F);
        poseStack.translate(0.0, 0.0, 0.0);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, entity, renderTypeNew, bufferSource.getBuffer(renderTypeNew), partialTick, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
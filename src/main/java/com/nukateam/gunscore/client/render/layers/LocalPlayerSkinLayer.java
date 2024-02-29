package com.nukateam.gunscore.client.render.layers;

import com.ibm.icu.impl.Pair;
import com.jetug.chassis_core.ChassisCore;
import com.jetug.chassis_core.client.render.layers.LayerBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import static com.jetug.chassis_core.common.util.helpers.TextureHelper.getTextureSize;

public class LocalPlayerSkinLayer<T extends GeoAnimatable> extends LayerBase<T> {
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

        var texture = ChassisCore.SKIN_STORAGE.getSkin(player, size);

        if (texture != null) {
            renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);
        }
    }
}
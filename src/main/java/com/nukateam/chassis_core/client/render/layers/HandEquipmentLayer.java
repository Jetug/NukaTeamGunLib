package com.nukateam.chassis_core.client.render.layers;

import com.nukateam.chassis_core.client.animators.HandAnimator;
import com.nukateam.chassis_core.client.render.layers.LayerBase;
import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

@SuppressWarnings("ConstantConditions")
public class HandEquipmentLayer<T extends HandAnimator> extends LayerBase<T> {
    public HandEquipmentLayer(GeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, T hand, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if(!PlayerUtils.isLocalWearingChassis()) return;
        var entity = PlayerUtils.getLocalPlayerChassis();

        poseStack.pushPose();
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        for (var part : entity.getEquipment()) {
            if (entity.isEquipmentVisible(part)) {
                var stack = entity.getEquipment(part);
                var item = entity.getEquipmentItem(part);

                if (item.getConfig() == null) return;
                var texture = item.getTexture(stack);

                if (texture == null) return;
//                    render(poseStack, bufferSource, packedLight, entity, partialTick, texture);
                renderLayer(poseStack, hand, bakedModel, bufferSource, partialTick, packedLight, texture);
            }
        }

        poseStack.popPose();
    }
}
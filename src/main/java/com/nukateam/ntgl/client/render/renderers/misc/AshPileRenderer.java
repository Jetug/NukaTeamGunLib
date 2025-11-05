package com.nukateam.ntgl.client.render.renderers.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.model.misc.AshPileModel;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AshPileRenderer extends GeoEntityRenderer<AshPile> {
    public AshPileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AshPileModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, AshPile entity, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                               float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        var prog = ((float) entity.getLife() / (float) entity.getMaxLife());

        var newAlpha = alpha;
        if (prog <= 0.2) {
            var maxAlpha = ((entity.getMaxLife() * 0.2f));
            var newProg = ((float) entity.getLife() / maxAlpha);
            newAlpha = newProg;
        }
        poseStack.pushPose();
        {
//            poseStack.scale(1.5f, 1.5f, 1.5f);
//            poseStack.translate(0, 1f / 16D, 0);
            poseStack.scale(newAlpha, newAlpha, newAlpha);
            RenderSystem.setShaderColor(1, 1, 1, newAlpha);
            super.actuallyRender(poseStack, animatable, model,
                    renderType, bufferSource, buffer,
                    isReRender, partialTick, packedLight,
                    packedOverlay, red, green, blue, newAlpha);

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        poseStack.popPose();
    }
}
package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

public class ModelGibsQuadruped extends ModelGibs {
    public QuadrupedModel model;

    public ModelGibsQuadruped(QuadrupedModel model) {
        this.model = model;
        var accessor = (IModelAccessor)model;
        accessor.getModelParts().forEach(part -> part.setRotation(0.0f, 0.0f, 0.0f));
    }

    @Override
    public void render(Entity entity, int part, PoseStack poseStack, RenderType rendertype, MultiBufferSource buffer, VertexConsumer pVertexConsumer, int packedLight, int packedOverlay) {
        var accessor = (IModelAccessor)model;
        accessor.getModelParts().get(part).render(poseStack, pVertexConsumer, packedLight, packedOverlay);
    }

    @Override
    public int getNumGibs() {
        return 6;
    }
}

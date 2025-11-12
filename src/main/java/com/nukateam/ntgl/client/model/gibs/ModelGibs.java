package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.util.data.Rgba;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

public abstract class ModelGibs {
    public abstract void render(Entity entity, int part, PoseStack poseStack, RenderType rendertype, MultiBufferSource buffer,
                                VertexConsumer pVertexConsumer, int packedLight, int packedOverlay, int colour);

    public abstract int getNumGibs();
}

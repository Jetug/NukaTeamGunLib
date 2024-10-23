package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class ModelGibsGeo extends ModelGibs {
    private ArrayList<CoreGeoBone> gibs = new ArrayList<>();
    private final BakedGeoModel model;
    private final GeoEntityRenderer geoRenderer;

    public ModelGibsGeo(BakedGeoModel model, GeoEntityRenderer geoRenderer) {
        this.model = model;
        this.geoRenderer = geoRenderer;
        var topBones = model.getBones();

        for (var bone: topBones) {
            var children = bone.getChildBones();
            gibs.addAll(children);
        }
    }

    @Override
    public void render(Entity entity, int part, PoseStack poseStack, RenderType rendertype, MultiBufferSource buffer, VertexConsumer pVertexConsumer, int packedLight, int packedOverlay) {
        var bone = (GeoBone) gibs.get(part);
        var vertexConsumer = buffer.getBuffer(rendertype);
        var partialTick = Minecraft.getInstance().getFrameTime();

        geoRenderer.renderRecursively(
                poseStack, entity, bone,
                rendertype, buffer, vertexConsumer,
                false, partialTick, packedLight, packedOverlay,
                1,1,1,1
        );
    }

    @Override
    public int getNumGibs() {
        return gibs.size();
    }
}

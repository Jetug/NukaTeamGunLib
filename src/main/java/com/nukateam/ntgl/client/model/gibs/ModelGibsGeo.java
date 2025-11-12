package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.util.data.Rgba;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class ModelGibsGeo extends ModelGibs {
    private ArrayList<GeoBone> gibs = new ArrayList<>();
    private final GeoEntityRenderer geoRenderer;

    public ModelGibsGeo(BakedGeoModel model, GeoEntityRenderer geoRenderer) {
        this.geoRenderer = geoRenderer;
        var topBones = model.topLevelBones();

        for (var bone: topBones) {
            var children = bone.getChildBones();
            gibs.addAll(children);
        }
    }

    @Override
    public void render(Entity entity, int part, PoseStack poseStack, RenderType rendertype, MultiBufferSource buffer,
                       VertexConsumer pVertexConsumer, int packedLight, int packedOverlay, int colour) {
        var bone = (GeoBone) gibs.get(part);
        var vertexConsumer = buffer.getBuffer(rendertype);
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        geoRenderer.renderRecursively(
                poseStack, entity, bone,
                rendertype, buffer, vertexConsumer,
                false, partialTick, packedLight, packedOverlay, colour
        );
    }

    @Override
    public int getNumGibs() {
        return gibs.size();
    }
}

package com.nukateam.chassis_core.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

import static com.nukateam.chassis_core.common.data.constants.Bones.LEFT_HAND;
import static com.nukateam.chassis_core.common.data.constants.Bones.RIGHT_HAND;

public class HeldItemLayer<T extends GeoAnimatable> extends BlockAndItemGeoLayer<T> {
    public HeldItemLayer(GeoRenderer<T> renderer, BiFunction<GeoBone, T, ItemStack> stackForBone) {
        super(renderer, stackForBone, (i, g) -> null);
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      T animatable, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        {
            poseStack.scale(-1,1,1);
            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource,
                    partialTick, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, GeoAnimatable animatable) {
        return switch (bone.getName()) {
            case RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            case LEFT_HAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            default -> ItemDisplayContext.NONE;
        };
    }
}

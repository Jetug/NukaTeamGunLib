package com.nukateam.gunscore.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.gunscore.client.model.GeoGunModel;
import com.nukateam.gunscore.common.foundation.item.GunItem;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

import static com.nukateam.gunscore.client.render.Render.GUN_RENDERER;

public class GunItemRenderer extends GeoItemRenderer<GunItem> {
    public GunItemRenderer() {
        super(new GeoGunModel<>());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var player = Minecraft.getInstance().player;

        GUN_RENDERER.render(
                player,
                stack,
                transformType,
                poseStack,
                bufferSource,
                null,
                null,
                packedLight);
    }
}

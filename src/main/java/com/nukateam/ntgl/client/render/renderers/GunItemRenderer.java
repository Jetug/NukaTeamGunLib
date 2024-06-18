package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.animators.GunItemAnimator;
import com.nukateam.ntgl.client.model.GeoGunModel;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GunItemRenderer extends GeoItemRenderer<GunItem> {
    private final DynamicGunRenderer<GunItemAnimator> renderer;

    public GunItemRenderer(DynamicGunRenderer<GunItemAnimator> renderer) {
        super(new GeoGunModel<>());
        this.renderer = renderer;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var player = Minecraft.getInstance().player;

        renderer.render(
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

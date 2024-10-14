package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.model.GeoPlaceholderModel;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GunItemRenderer extends GeoItemRenderer<GunItem> {
    private final DynamicGunRenderer<GunAnimator> renderer;

    public GunItemRenderer(DynamicGunRenderer<GunAnimator> renderer) {
        super(new GeoPlaceholderModel<>());
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

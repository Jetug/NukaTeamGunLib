package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.model.GunPlaceholderModel;
import com.nukateam.ntgl.client.model.PlaceholderModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ProxyItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private final DynamicGeoItemRenderer<GunAnimator> renderer;

    public ProxyItemRenderer(DynamicGeoItemRenderer<GunAnimator> renderer) {
        super(new PlaceholderModel<>());
        this.renderer = renderer;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var player = Minecraft.getInstance().player;
        if(renderer == null) return;

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

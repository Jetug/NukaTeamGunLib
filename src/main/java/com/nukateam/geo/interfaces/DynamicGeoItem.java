package com.nukateam.geo.interfaces;

import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.geo.render.ProxyItemRenderer;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.client.RenderProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface DynamicGeoItem extends GeoItem {
    @OnlyIn(Dist.CLIENT)
    DynamicGeoItemRenderer getRenderer();

    <Animator extends ItemAnimator> BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<Animator>, Animator> getAnimatorFactory();

    @Override
    default void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private ProxyItemRenderer renderer = null;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    return new ProxyItemRenderer(getRenderer());
                return this.renderer;
            }
        });
    }
}
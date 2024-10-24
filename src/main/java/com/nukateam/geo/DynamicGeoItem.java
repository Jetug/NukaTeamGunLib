package com.nukateam.geo;

import com.nukateam.ntgl.client.render.renderers.DynamicGunRenderer;
import com.nukateam.ntgl.client.render.renderers.GeoDynamicItemRenderer;
import com.nukateam.ntgl.client.render.renderers.GunItemRenderer;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public interface DynamicGeoItem extends GeoItem {
    @OnlyIn(Dist.CLIENT)
    GeoDynamicItemRenderer getRenderer();

//    @Override
//    default void createRenderer(Consumer<Object> consumer) {
//        consumer.accept(new RenderProvider() {
//            private GunItemRenderer renderer = null;
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (renderer == null)
//                    return new GunItemRenderer(getRenderer());
//                return this.renderer;
//            }
//        });
//    }
}

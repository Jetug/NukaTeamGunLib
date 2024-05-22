package com.nukateam.example.common.items;

import com.nukateam.ntgl.client.render.renderers.GunItemRenderer;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

import java.util.function.Consumer;

import static com.nukateam.ntgl.client.render.Render.GUN_RENDERER;

public class MinigunItem extends GunItem {
    public MinigunItem(Properties properties) {
        super(properties);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GunItemRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    return new GunItemRenderer(GUN_RENDERER);
                return this.renderer;
            }
        });
    }
}

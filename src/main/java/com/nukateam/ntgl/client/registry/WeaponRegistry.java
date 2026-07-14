package com.nukateam.ntgl.client.registry;

import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.*;
import com.nukateam.ntgl.client.animators.*;
import com.nukateam.ntgl.client.render.renderers.weapon.*;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class WeaponRegistry {
    private static final Map<Item, DynamicGeoItemRenderer> RENDERERS = new HashMap<>();
    private static final Map<Item, BiFunction<ItemDisplayContext, ?, ?>>
            ANIMATORS = new HashMap<>();

    public static DynamicGeoItemRenderer getRenderer(Item item) {
        return RENDERERS.computeIfAbsent(item, (i) -> new DefaultWeaponRendererGeo());
    }

    public static void registerRenderer(Item item, DynamicGeoItemRenderer renderer) {
        RENDERERS.put(item, renderer);
    }

    public static BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<?>, ItemAnimator> getAnimator(Item item) {
        return (BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<?>, ItemAnimator>) ANIMATORS.computeIfAbsent(item,
                (i) -> getAnimatorFactory());
    }

    public static BiFunction<ItemDisplayContext, DynamicGeoItemRenderer, ItemAnimator> getAnimatorFactory() {
        return WeaponAnimator::new;
    }

    public static <Animator extends ItemAnimator> void registerAnimator(Item item, BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<Animator>, Animator> renderer) {
        ANIMATORS.put(item, renderer);
    }
}

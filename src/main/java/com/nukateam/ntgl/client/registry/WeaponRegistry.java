package com.nukateam.ntgl.client.registry;

import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.*;
import com.nukateam.ntgl.client.animators.*;
import com.nukateam.ntgl.client.render.renderers.weapon.DefaultWeaponRendererGeo;
import com.nukateam.ntgl.client.render.renderers.weapon.DynamicWeaponRenderer;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class WeaponRegistry {
    private static final Map<Item, DynamicWeaponRenderer> RENDERERS = new HashMap<>();
    private static final Map<Item, BiFunction<ItemDisplayContext, ?, ?>>
            ANIMATORS = new HashMap<>();

    public static DynamicWeaponRenderer getRenderer(Item item) {
        return RENDERERS.computeIfAbsent(item, (i) -> new DefaultWeaponRendererGeo());
    }

    public static void registerRenderer(Item item, DynamicWeaponRenderer renderer) {
        RENDERERS.put(item, renderer);
    }

    public static BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<?>, WeaponAnimator> getAnimator(Item item) {
        return (BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<?>, WeaponAnimator>) ANIMATORS.computeIfAbsent(item,
                (i) -> getAnimatorFactory());

//        registerAnimator(new Item(null), SusAnimator::new);
    }


    public static BiFunction<ItemDisplayContext, DynamicWeaponRenderer, WeaponAnimator> getAnimatorFactory() {
        return WeaponAnimator::new;
    }

    public static <Animator extends WeaponAnimator> void registerAnimator(Item item, BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<Animator>, Animator> renderer) {
        ANIMATORS.put(item, renderer);
    }
}

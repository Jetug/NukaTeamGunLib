package com.nukateam.ntgl.client.registry;

import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
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
    private static final Map<Item, DynamicWeaponRenderer<?>> RENDERERS = new HashMap<>();
    private static final Map<Item, BiFunction<ItemDisplayContext, DynamicWeaponRenderer<WeaponAnimator>, WeaponAnimator>>
            ANIMATORS = new HashMap<>();

    public static DynamicWeaponRenderer<?> getRenderer(Item item) {
        return RENDERERS.computeIfAbsent(item, (i) -> new DefaultWeaponRendererGeo());
    }

    public static void registerRenderer(Item item, DynamicWeaponRenderer<?> renderer) {
        RENDERERS.put(item, renderer);
    }

    public static BiFunction<ItemDisplayContext, DynamicWeaponRenderer<WeaponAnimator>, WeaponAnimator> getAnimator(Item item) {
        return ANIMATORS.get(item);
    }

    public static void registerAnimator(Item item, BiFunction<ItemDisplayContext, DynamicWeaponRenderer<WeaponAnimator>, WeaponAnimator> renderer) {
        ANIMATORS.put(item, renderer);
    }
}

package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

public class AmmoHolder extends ResourceHolder {
    private static final Map<ResourceLocation, AmmoHolder> ammoTypeMap = Collections.synchronizedMap(new HashMap<>());

    private static final ResourceLocation EMPTY_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "air");

    private static final Map<ResourceLocation, AmmoHolder> fallbackCache = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ResourceLocation, AmmoHolder> eldest) {
                    return size() > 64;
                }
            });

    private boolean canReturnAmmo = false;
    private Function<ItemStack, Boolean> isAcceptable = (stack) -> false;
    private Function<ItemStack, Integer> getValue = (stack) -> 1;
    private BiFunction<ItemStack, Integer, List<ItemStack>> onConsume = (stack, i) -> List.of();

    private Function<AmmoHolder, String> getDescriptionId = (ammo) ->
            "info." + ammo.id.getNamespace() + "." + ammo.id.getPath();

    public AmmoHolder(ResourceLocation id) {
        super(id);
    }

    public boolean canReturnAmmo() {
        return canReturnAmmo;
    }

    public boolean isAcceptable(ItemStack ammoStack) {
        return isAcceptable.apply(ammoStack);
    }

    public int getValue(ItemStack ammoStack) {
        return getValue.apply(ammoStack);
    }

    public BiFunction<ItemStack, Integer, List<ItemStack>> onConsume() {
        return onConsume;
    }

    public String getDescriptionId() {
        return getDescriptionId.apply(this);
    }

    public static void registerType(AmmoHolder mode) {
        ammoTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoHolder getType(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            Ntgl.LOGGER.error("Malformed ammo id '{}', falling back to {}", id, EMPTY_ID);
            return getType(EMPTY_ID);
        }
        return getType(parsed);
    }

    public static AmmoHolder getType(ResourceLocation id) {
        if (id == null) {
            Ntgl.LOGGER.error("Null ammo id passed to getType, falling back to {}", EMPTY_ID);
            return getType(EMPTY_ID);
        }

        AmmoHolder registered = ammoTypeMap.get(id);
        if (registered != null) {
            return registered;
        }

        AmmoHolder cached = fallbackCache.get(id);
        if (cached != null) {
            return cached;
        }

        if (!id.equals(EMPTY_ID)) {
            Ntgl.LOGGER.debug("Ammo id '{}' is not registered, using a default holder", id);
        }
        AmmoHolder fallback = createDefault(id);
        fallbackCache.put(id, fallback);
        return fallback;
    }

    private static AmmoHolder createDefault(ResourceLocation id){
        var holder = Builder.create(id)
                .isAcceptable((stack) -> Objects.equals(getKey(stack), id))
                .value((s) -> 1)
                .descriptionId((ammo) -> {
                    var item = BuiltInRegistries.ITEM.get(ammo.getId());
                    if (item != null){
                        return item.getDescriptionId();
                    }
                    return "---";
                })
                .canReturnAmmo()
                .build();

        return holder;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else return other instanceof AmmoHolder holder
                    && id.getNamespace().equals(holder.id.getNamespace())
                    && this.id.getPath().equals(holder.id.getPath());
    }

    private static @Nullable ResourceLocation getKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static class Builder {
        private final AmmoHolder holder;

        public Builder(AmmoHolder holder) {
            this.holder = holder;
        }

        public static Builder create(ResourceLocation id) {
            var holder = new AmmoHolder(id);
            return new Builder(holder);
        }

        public Builder isAcceptable(Function<ItemStack, Boolean> isAcceptable) {
            holder.isAcceptable = isAcceptable;
            return this;
        }

        public Builder value(Function<ItemStack, Integer> getValue) {
            holder.getValue = getValue;
            return this;
        }

        public Builder onConsume(BiFunction<ItemStack, Integer, List<ItemStack>> onConsume) {
            holder.onConsume = onConsume;
            return this;
        }

        public Builder descriptionId(Function<AmmoHolder, String > getValue) {
            holder.getDescriptionId = getValue;
            return this;
        }

        public Builder canReturnAmmo() {
            holder.canReturnAmmo = true;
            return this;
        }

        public AmmoHolder build() {
            return holder;
        }

    }
}

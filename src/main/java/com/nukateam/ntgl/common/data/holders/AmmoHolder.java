package com.nukateam.ntgl.common.data.holders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

public class AmmoHolder extends ResourceHolder {
    private static final Map<ResourceLocation, AmmoHolder> ammoTypeMap = new HashMap<>();

    private boolean canReturnAmmo = false;
    private Function<ItemStack, Boolean> isAcceptable = (stack) -> false;
    private Function<ItemStack, Integer> getValue = (stack) -> 1;
    private BiFunction<ItemStack, Integer, List<ItemStack>> onConsume = (stack, i) -> {
        return List.of();
    };

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
        return getType(ResourceLocation.tryParse(id));
    }

    public static AmmoHolder getType(ResourceLocation id) {
        return ammoTypeMap.getOrDefault(id, createDefault(id));
    }

    private static AmmoHolder createDefault(ResourceLocation id){
        var holder = Builder.create(id)
                .isAcceptable((stack) -> Objects.equals(getKey(stack), id))
                .value((s) -> 1)
                .descriptionId((ammo) -> {
                    var item = ForgeRegistries.ITEMS.getValue(ammo.getId());
                    if (item != null){
                        return item.getDescriptionId();
                    }
                    return "---";
                })
                .canReturnAmmo()
                .build();

        registerType(holder);
        return holder;
    }

    private static @Nullable ResourceLocation getKey(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
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

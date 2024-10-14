package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableMap;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.IConfigConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public abstract class NetworkManager<T extends IConfigConsumer, Y extends INBTSerializable<CompoundTag>> extends SimplePreparableReloadListener<Map<T, Y>> {
    private Map<ResourceLocation, Y> registeredGuns = new HashMap<>();

    protected abstract Boolean check(Item v);
    protected abstract Class<Y> getConfigClass();
    protected abstract String getPath();

    @Override
    protected Map<T, Y> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, this::check, getConfigClass(), getPath());
    }

    @Override
    protected void apply(Map<T, Y> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, Y>builder();

        objects.forEach((configProvider, gun) -> {
            var item = (Item)configProvider;
            Validate.notNull(ITEMS.getKey(item));
            builder.put(ITEMS.getKey(item), gun);
            configProvider.setConfig(new NetworkManager.Supplier<>(gun));
        });

        this.registeredGuns = builder.build();
    }

    /**
     * A simple wrapper for a gun object to pass to GunItem. This is to indicate to developers that
     * Gun instances shouldn't be changed on GunItems as they are controlled by NetworkGunManager.
     * Changes to gun properties should be made through the JSON file.
     */
    public static class Supplier<S extends INBTSerializable<CompoundTag>> {
        private final S config;

        Supplier(S config) {
            this.config = config;
        }

        public S getConfig() {
            return this.config;
        }
    }
}

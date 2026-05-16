package com.nukateam.ntgl.modules.datapack.managers;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.modules.config.annotation.Validator;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.data.json.JsonDeserializers;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.modules.constants.Paths;
import com.nukateam.ntgl.modules.datapack.ConfigUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkProjectileManager extends SimplePreparableReloadListener<Map<ResourceLocation, ProjectileConfig>> {
    private static NetworkProjectileManager instance;
    private Map<ResourceLocation, ProjectileConfig> PROGECTILE_CONFIGS = new HashMap<>();

    @Nullable
    public static NetworkProjectileManager get() {
        return instance;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkProjectileManager networkGunManager = new NetworkProjectileManager();
        event.addListener(networkGunManager);
        NetworkProjectileManager.instance = networkGunManager;
    }

    public static void onServerStopped() {
        NetworkProjectileManager.instance = null;
    }

    @NotNull
    private static Map<ResourceLocation, Resource> getJsonResources(ResourceManager manager, String path) {
        return manager.listResources(path, (fileName) -> fileName.getPath().endsWith(".json"));
    }

    @Override
    protected Map<IAmmo, ProjectileConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, BuiltInRegistries.ITEM, (v) -> v instanceof IAmmo, ProjectileConfig.class, Paths.PROJECTILES);
    }

    @Override
    protected void apply(Map<ResourceLocation, ProjectileConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, ProjectileConfig>builder();
        builder.putAll(objects);
        PROGECTILE_CONFIGS = builder.build();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(PROGECTILE_CONFIGS.size());
        PROGECTILE_CONFIGS.forEach((id, ammo) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(ammo.serializeNBT(null));
        });
    }

    public ProjectileConfig getConfig(ResourceLocation id) {
        return PROGECTILE_CONFIGS.get(id);
    }

    public static ImmutableMap<ResourceLocation, ProjectileConfig> read(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            var builder = ImmutableMap.<ResourceLocation, ProjectileConfig>builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var ammo = ProjectileConfig.create(buffer.readNbt());
                builder.put(id, ammo);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    /**
     * Updates registered projectile from data provided by the server
     */
    public void update(Map<ResourceLocation, ProjectileConfig> registeredAmmo) {
        if (registeredAmmo != null) {
            PROGECTILE_CONFIGS = registeredAmmo;
        }
    }

    public static class Supplier {
        private ProjectileConfig projectile;

        private Supplier(ProjectileConfig projectile) {
            this.projectile = projectile;
        }

        public ProjectileConfig getAmmo() {
            return this.projectile;
        }
    }
}

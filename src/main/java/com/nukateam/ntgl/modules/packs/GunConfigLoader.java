package com.nukateam.ntgl.modules.packs;

import com.google.gson.Gson;
import com.nukateam.ntgl.common.base.ConfigUtils;
import com.nukateam.ntgl.common.data.config.gun.CustomGun;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.nukateam.ntgl.common.base.ConfigUtils.GSON_INSTANCE;

public class GunConfigLoader implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
            var resources = manager.listResources("guns", r -> r.getPath().endsWith(".json"));

            for (var entry : resources.entrySet()) {
                try (var stream = entry.getValue().open()) {
                    var gunConfig = GSON_INSTANCE.fromJson(new InputStreamReader(stream), Gun.class);

                    var gunId = ResourceLocation.tryBuild(
                            entry.getKey().getNamespace(),
                            entry.getKey().getPath().replace("guns/", "").replace(".json", "")
                    );

                    createGunItem(gunId, gunConfig);
                } catch (Exception e) {
                    GunPackModule.LOGGER.error("Failed to load gun config: {}", entry.getKey(), e);
                }
            }
        }, backgroundExecutor).thenCompose(stage::wait);
    }

    private static void createGunItem(ResourceLocation id, Gun gunConfig) {
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            Item.Properties properties = new Item.Properties().stacksTo(1);
            GunItem gunItem = new GunItem(properties);
            GeneratedGunItems.GUN_ITEMS.put(id, gunItem);
            GeneratedGunItems.ITEMS.register(id.getPath(), () -> gunItem);
        }
    }
//
//    @Override
//    public CompletableFuture<Void> prepare(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
//        return CompletableFuture.completedFuture(null);
//    }

    @Override public String getName(){
        return "Gun Config Loader";
    }
}
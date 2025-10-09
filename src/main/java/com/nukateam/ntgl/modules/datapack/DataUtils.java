package com.nukateam.ntgl.modules.datapack;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.json.JsonDeserializers;
import com.nukateam.ntgl.common.util.annotation.Validator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public final class DataUtils {
    private static final Registry<Item> ITEMS = BuiltInRegistries.ITEM;
    private static final int JSON_EXT_LEN = ".json".length();

    public static <Y> Map<Item, Y> getConfigMap(ResourceManager manager,
                                                Predicate<Item> filter,
                                                Class<Y> yClass,
                                                String resourcePath) {
        Map<Item, Y> result = new HashMap<>();

        ITEMS.stream()
                .filter(filter)
                .forEach(item -> {
                    ResourceLocation id = ITEMS.getKey(item);
                    Map<ResourceLocation, Resource> candidates = getJsonResources(manager, resourcePath, id);

                    List<ResourceLocation> sorted = candidates.keySet()
                            .stream()
                            .sorted(Comparator.<ResourceLocation, Boolean>comparing(
                                    r -> r.getNamespace().equals(Ntgl.MOD_ID)).reversed())
                            .toList();

                    for (ResourceLocation rl : sorted) {
                        String path = rl.getPath();
                        String fileName = path.substring(path.lastIndexOf('/') + 1, path.length() - JSON_EXT_LEN);

                        if (!id.getPath().equals(fileName) || !id.getNamespace().equals(rl.getNamespace()))
                            continue;

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(manager.getResource(rl).orElseThrow().open(), StandardCharsets.UTF_8))) {

                            Y object = GsonHelper.fromJson(JsonDeserializers.GSON_INSTANCE, reader, yClass);
                            if (Validator.isValidObject(object)) {
                                result.put(item, object);
                            } else {
                                Ntgl.LOGGER.warn("Invalid data in {} – using default", rl);
                                result.putIfAbsent(item, yClass.getDeclaredConstructor().newInstance());
                            }
                        } catch (Exception ex) {
                            Ntgl.LOGGER.error("Failed to load {}", rl, ex);
                        }
                    }
                });
        return result;
    }

    @NotNull
    private static Map<ResourceLocation, Resource> getJsonResources(ResourceManager manager,
                                                                    String path,
                                                                    ResourceLocation id) {
        return manager.listResources(path,
                file -> file.getPath().endsWith(id.getPath() + ".json"));
    }
}

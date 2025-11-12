package com.nukateam.chassis_core.modules.config.utils;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.config.annotation.Validator;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigUtils {
    private static final int FILE_TYPE_LENGTH_VALUE = ".json".length();

    @NotNull
    private static Map<ResourceLocation, Resource> getJsonResources(ResourceManager manager, String path, ResourceLocation id) {
        return manager.listResources(path, (fileName) -> fileName.getPath().endsWith(id.getPath() + ".json"));
    }

    public static <T, Y, R> Map<T, Y> getConfigMap(ResourceManager manager,
                                                   Iterable<R> registry,
                                                   Function<R, Boolean> filter,
                                                   Class<Y> yClass,
                                                   String resourcePath) {
        var map = new HashMap<T, Y>();

        for (R item : registry) {
            if (!filter.apply(item)) continue;

            var id = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>) item);

            if (id != null) {
                var resources = new ArrayList<>(getJsonResources(manager, resourcePath, id).keySet());

                resources.sort((r1, r2) -> {
                    if (r1.getNamespace().equals(r2.getNamespace())) return 0;
                    return r2.getNamespace().equals(ChassisCore.MOD_ID) ? 1 : -1;
                });

                for (ResourceLocation resourceLocation : resources) {
                    var pathStr = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - FILE_TYPE_LENGTH_VALUE);
                    var splitPath = pathStr.split("/");

                    // Makes sure the file name matches exactly with the id of the object
                    if (!id.getPath().equals(splitPath[splitPath.length - 1]))
                        continue;

                    // Also check if the mod id matches with the object's registered namespace
                    if (!id.getNamespace().equals(resourceLocation.getNamespace()))
                        continue;

                    manager.getResource(resourceLocation).ifPresent(resource ->
                    {
                        try (var reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                            var gun = GsonHelper.fromJson(JsonDeserializers.GSON_INSTANCE, reader, yClass);

                            if (Validator.isValidObject(gun)) {
                                map.put((T) item, gun);
                            }
                            else {
                                ChassisCore.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
                                try {
                                    map.putIfAbsent((T) item, yClass.getDeclaredConstructor().newInstance());
                                } catch (Exception e) {
                                    ChassisCore.LOGGER.error("Failed to create default instance for {}", resourceLocation);
                                }
                            }
                        }
                        catch (InvalidObjectException e) {
                            Ntgl.LOGGER.error("Missing required properties for {}", resourceLocation);
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            Ntgl.LOGGER.error("Couldn't parse data file {}", resourceLocation);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        return map;
    }
}

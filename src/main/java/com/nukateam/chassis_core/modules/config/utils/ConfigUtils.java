package com.nukateam.chassis_core.modules.config.utils;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.config.annotation.Validator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;
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

//    @NotNull
//    public static<T, Y> Map<T, Y> getConfigMap(ResourceManager manager, Function<Item, Boolean> tClass, Class<Y> yClass, String resourcePath) {
//        var map = new HashMap<T, Y>();
//
////        var registry = ForgeRegistries.ITEMS;
//        var registry = ForgeRegistries.ENTITY_TYPES;
//
//        registry.getValues().stream().filter(tClass::apply).forEach(item ->
//        {
//            var id = registry.getKey(item);
//
//            if (id != null) {
//                var resources = new ArrayList<>(getJsonResources(manager, resourcePath, id).keySet());
//
//                resources.sort((r1, r2) -> {
//                    if (r1.getNamespace().equals(r2.getNamespace())) return 0;
//                    return r2.getNamespace().equals(ChassisCore.MOD_ID) ? 1 : -1;
//                });
//
//                resources.forEach(resourceLocation ->
//                {
//                    var path = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - FILE_TYPE_LENGTH_VALUE);
//                    var splitPath = path.split("/");
//
//                    // Makes sure the file name matches exactly with the id of the gun
//                    if (!id.getPath().equals(splitPath[splitPath.length - 1]))
//                        return;
//
//                    // Also check if the mod id matches with the gun's registered namespace
//                    if (!id.getNamespace().equals(resourceLocation.getNamespace()))
//                        return;
//
//                    manager.getResource(resourceLocation).ifPresent(resource ->
//                    {
//                        try (var reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
//                            var gun = GsonHelper.fromJson(JsonDeserializers.GSON_INSTANCE, reader, yClass);
//
//                            if (Validator.isValidObject(gun)) {
//                                map.put((T) item, gun);
//                            }
//                            else {
//                                ChassisCore.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
//                                map.putIfAbsent((T) item, yClass.getDeclaredConstructor().newInstance());
//                            }
//                        }
//                        catch (InvalidObjectException e) {
//                            ChassisCore.LOGGER.error("Missing required properties for {}", resourceLocation);
//                            e.printStackTrace();
//                        }
//                        catch (IOException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
//                            ChassisCore.LOGGER.error("Couldn't parse data file {}", resourceLocation);
//                        }
//                        catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                });
//            }
//        });
//        return map;
//    }
//

    public static<T, Y, R> Map<T, Y> getConfigMap(ResourceManager manager, IForgeRegistry<R> registry, Function<R, Boolean> tClass, Class<Y> yClass, String resourcePath) {
        var map = new HashMap<T, Y>();

        registry.getValues().stream().filter(tClass::apply).forEach(item ->
        {
            var id = registry.getKey(item);

            if (id != null) {
                var resources = new ArrayList<>(getJsonResources(manager, resourcePath, id).keySet());

                resources.sort((r1, r2) -> {
                    if (r1.getNamespace().equals(r2.getNamespace())) return 0;
                    return r2.getNamespace().equals(ChassisCore.MOD_ID) ? 1 : -1;
                });

                resources.forEach(resourceLocation ->
                {
                    var path = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - FILE_TYPE_LENGTH_VALUE);
                    var splitPath = path.split("/");

                    // Makes sure the file name matches exactly with the id of the gun
                    if (!id.getPath().equals(splitPath[splitPath.length - 1]))
                        return;

                    // Also check if the mod id matches with the gun's registered namespace
                    if (!id.getNamespace().equals(resourceLocation.getNamespace()))
                        return;

                    manager.getResource(resourceLocation).ifPresent(resource ->
                    {
                        try (var reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                            var gun = GsonHelper.fromJson(JsonDeserializers.GSON_INSTANCE, reader, yClass);

                            if (Validator.isValidObject(gun)) {
                                map.put((T) item, gun);
                            }
                            else {
                                ChassisCore.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
                                map.putIfAbsent((T) item, yClass.getDeclaredConstructor().newInstance());
                            }
                        }
                        catch (InvalidObjectException e) {
                            ChassisCore.LOGGER.error("Missing required properties for {}", resourceLocation);
                            e.printStackTrace();
                        }
                        catch (IOException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                            ChassisCore.LOGGER.error("Couldn't parse data file {}", resourceLocation);
                        }
                        catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }
        });
        return map;
    }
}

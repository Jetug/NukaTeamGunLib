package com.nukateam.ntgl.common.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.util.Easings;
import com.nukateam.ntgl.common.base.gun.*;
import com.nukateam.ntgl.common.base.utils.JsonDeserializers;
import com.nukateam.ntgl.common.data.annotation.Validator;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class ConfigUtils {
    private static final int FILE_TYPE_LENGTH_VALUE = ".json".length();

    public static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.registerTypeAdapter(GripType.class, JsonDeserializers.GRIP_TYPE);
        builder.registerTypeAdapter(LoadingType.class, JsonDeserializers.LOADING_TYPE);
        builder.registerTypeAdapter(FireMode.class, JsonDeserializers.FIRE_MODE);
        builder.registerTypeAdapter(AttachmentType.class, JsonDeserializers.ATTACHMENT_TYPE);
        builder.registerTypeAdapter(AmmoType.class, JsonDeserializers.AMMO_TYPE);
        builder.registerTypeAdapter(Easings.class, JsonDeserializers.EASING);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });

    @NotNull
    private static Map<ResourceLocation, Resource> getJsonResources(ResourceManager manager, String path, ResourceLocation id) {
        return manager.listResources(path, (fileName) -> fileName.getPath().endsWith(id.getPath() + ".json"));
    }

    @NotNull
    public static<T, Y> Map<T, Y> getConfigMap(ResourceManager manager, Function<Item, Boolean> tClass, Class<Y> yClass, String resourcePath) {
        var map = new HashMap<T, Y>();

        ITEMS.getValues().stream().filter(tClass::apply).forEach(item ->
        {
            var id = ITEMS.getKey(item);

            if (id != null) {
                var resources = new ArrayList<>(getJsonResources(manager, resourcePath, id).keySet());

                resources.sort((r1, r2) -> {
                    if (r1.getNamespace().equals(r2.getNamespace())) return 0;
                    return r2.getNamespace().equals(Ntgl.MOD_ID) ? 1 : -1;
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
                            var gun = GsonHelper.fromJson(GSON_INSTANCE, reader, yClass);

                            if (Validator.isValidObject(gun)) {
                                map.put((T) item, gun);
                            }
                            else {
                                Ntgl.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
                                map.putIfAbsent((T) item, yClass.getDeclaredConstructor().newInstance());
                            }
                        }
                        catch (InvalidObjectException e) {
                            Ntgl.LOGGER.error("Missing required properties for {}", resourceLocation);
                            e.printStackTrace();
                        }
                        catch (IOException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                            Ntgl.LOGGER.error("Couldn't parse data file {}", resourceLocation);
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

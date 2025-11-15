
package com.nukateam.chassis_core.modules.config.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Modifier;

/**
 * Author: MrCrayfish
 */
public class JsonDeserializers {
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> ResourceLocation.parse(json.getAsString());
    public static final JsonDeserializer<ChassisPart> BODY_PART = (json, typeOfT, context) -> ChassisPart.getType(json.getAsString());

    public static final Gson GSON_INSTANCE = Util.make(() ->
        new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION)
                .registerTypeAdapter(ChassisPart.class, JsonDeserializers.BODY_PART)
                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .create()
    );
}

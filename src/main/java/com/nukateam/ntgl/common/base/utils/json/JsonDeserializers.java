package com.nukateam.ntgl.common.base.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.util.Easings;
import com.google.gson.JsonDeserializer;
import com.nukateam.ntgl.common.base.holders.*;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

public class JsonDeserializers {
    public static final JsonDeserializer<ItemStack> ITEM_STACK = (json, typeOfT, context) -> CraftingHelper.getItemStack(json.getAsJsonObject(), true);
    public static final JsonDeserializer<FireMode>  FIRE_MODE = (json, typeOfT, context) -> FireMode.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<AttachmentType> ATTACHMENT_TYPE = (json, typeOfT, context) -> AttachmentType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<AmmoType> AMMO_TYPE = (json, typeOfT, context) -> AmmoType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<ProjectileType> PROJECTILE_TYPE = (json, typeOfT, context) -> ProjectileType.getType(json.getAsString());
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> ResourceLocation.tryParse(json.getAsString());
    public static final JsonDeserializer<GripType> GRIP_TYPE = (json, typeOfT, context) -> GripType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<LoadingType> LOADING_TYPE = (json, typeOfT, context) -> LoadingType.getType(json.getAsString());
    public static final JsonDeserializer<FuelType> SECONDARY_AMMO_TYPE = (json, typeOfT, context) -> FuelType.getType(json.getAsString());
    public static final JsonDeserializer<ResourceKey<DamageType>> DAMAGE_TYPE = (json, typeOfT, context) -> getDamageTypeResourceKey(json.getAsString());

    public static @NotNull ResourceKey<DamageType> getDamageTypeResourceKey(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryParse(id));
    }

    public static final JsonDeserializer<Easings> EASING = (json, typeOfT, context) -> Easings.byName(json.getAsString());

    public static final Gson GSON_INSTANCE = Util.make(() -> {
        var builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, RESOURCE_LOCATION);
        builder.registerTypeAdapter(GripType.class, GRIP_TYPE);
        builder.registerTypeAdapter(LoadingType.class, LOADING_TYPE);
        builder.registerTypeAdapter(FuelType.class, SECONDARY_AMMO_TYPE);
        builder.registerTypeAdapter(FireMode.class, FIRE_MODE);
        builder.registerTypeAdapter(AttachmentType.class, ATTACHMENT_TYPE);
        builder.registerTypeAdapter(AmmoType.class, AMMO_TYPE);
        builder.registerTypeAdapter(ProjectileType.class, PROJECTILE_TYPE);
        builder.registerTypeAdapter(Easings.class, EASING);
        builder.registerTypeAdapter(ResourceKey.class, DAMAGE_TYPE);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });
}

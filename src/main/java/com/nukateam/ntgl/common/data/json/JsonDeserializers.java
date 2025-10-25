package com.nukateam.ntgl.common.data.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nukateam.ntgl.client.util.util.Easings;
import com.google.gson.JsonDeserializer;
import com.nukateam.ntgl.common.data.holders.*;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

public class JsonDeserializers {
    public static final JsonDeserializer<Easings> EASING = (json, typeOfT, context) -> Easings.byName(json.getAsString());
    public static final JsonDeserializer<FireMode>  FIRE_MODE = (json, typeOfT, context) -> FireMode.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<AttachmentType> ATTACHMENT_TYPE = (json, typeOfT, context) -> AttachmentType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<AmmoType> AMMO_TYPE = (json, typeOfT, context) -> AmmoType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<ProjectileType> PROJECTILE_TYPE = (json, typeOfT, context) -> ProjectileType.getType(json.getAsString());
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> ResourceLocation.tryParse(json.getAsString());
    public static final JsonDeserializer<GripType> GRIP_TYPE = (json, typeOfT, context) -> GripType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<LoadingType> LOADING_TYPE = (json, typeOfT, context) -> LoadingType.getType(json.getAsString());
    public static final JsonDeserializer<WeaponAction> WEAPON_TYPE = (json, typeOfT, context) -> WeaponAction.getType(json.getAsString());
    public static final JsonDeserializer<WeaponMode> WEAPON_MODE = (json, typeOfT, context) -> WeaponMode.getType(json.getAsString());
    public static final JsonDeserializer<MeleeMode> MELEE_MODE = (json, typeOfT, context) -> MeleeMode.getType(json.getAsString());
    public static final JsonDeserializer<ThrowMode> GRENADE_MODE = (json, typeOfT, context) -> ThrowMode.getType(json.getAsString());
    public static final JsonDeserializer<AmmoHolder> SECONDARY_AMMO_TYPE = (json, typeOfT, context) -> AmmoHolder.getType(json.getAsString());
    public static final JsonDeserializer<CounterType> COUNTER_TYPE = (json, typeOfT, context) -> CounterType.getType(json.getAsString());
    public static final JsonDeserializer<AnimationType> ANIMATION_TYPE = (json, typeOfT, context) -> AnimationType.getType(json.getAsString());
    public static final JsonDeserializer<CustomAttack> CUSTOM_ATTACK = (json, typeOfT, context) -> CustomAttack.getType(json.getAsString());
    public static final JsonDeserializer<ResourceKey<DamageType>> DAMAGE_TYPE = (json, typeOfT, context) -> getDamageTypeResourceKey(json.getAsString());

    public static final JsonDeserializer<Vec3> VECTOR = (json, typeOfT, context) -> {
        var array = json.getAsJsonArray();

        if(array.size() >= 3){
            var x = array.get(0).getAsDouble();
            var y = array.get(1).getAsDouble();
            var z = array.get(2).getAsDouble();
            return new Vec3(x, y, z);
        }

        return Vec3.ZERO;
    };

    public static final Gson GSON_INSTANCE = Util.make(() -> {
        var builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, RESOURCE_LOCATION);
        builder.registerTypeAdapter(GripType.class, GRIP_TYPE);
        builder.registerTypeAdapter(LoadingType.class, LOADING_TYPE);
        builder.registerTypeAdapter(WeaponAction.class, WEAPON_TYPE);
        builder.registerTypeAdapter(WeaponMode.class, WEAPON_MODE);
        builder.registerTypeAdapter(MeleeMode.class, MELEE_MODE);
        builder.registerTypeAdapter(ThrowMode.class, GRENADE_MODE);
        builder.registerTypeAdapter(AmmoHolder.class, SECONDARY_AMMO_TYPE);
        builder.registerTypeAdapter(FireMode.class, FIRE_MODE);
        builder.registerTypeAdapter(AttachmentType.class, ATTACHMENT_TYPE);
        builder.registerTypeAdapter(AmmoType.class, AMMO_TYPE);
        builder.registerTypeAdapter(ProjectileType.class, PROJECTILE_TYPE);
        builder.registerTypeAdapter(Easings.class, EASING);
        builder.registerTypeAdapter(ResourceKey.class, DAMAGE_TYPE);
        builder.registerTypeAdapter(CounterType.class, COUNTER_TYPE);
        builder.registerTypeAdapter(AnimationType.class, ANIMATION_TYPE);
        builder.registerTypeAdapter(CustomAttack.class, CUSTOM_ATTACK);
        builder.registerTypeAdapter(Vec3.class, VECTOR);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });

    public static @NotNull ResourceKey<DamageType> getDamageTypeResourceKey(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryParse(id));
    }
}

package com.nukateam.ntgl.client.util.helpers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.MetaLoader;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.properties.SightAnimation;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.data.ObjectCache;
import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.framework.api.serialize.DataArray;
import com.mrcrayfish.framework.api.serialize.DataNumber;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.api.serialize.DataType;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

//TODO eventually convert to deserialized objects

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("ConstantConditions")
public final class PropertyHelper {
    public static final String CACHE_KEY  = "properties";
    public static final String MODEL_KEY  = Ntgl.MOD_ID + ":model";
    public static final String WEAPON_KEY = Ntgl.MOD_ID + ":weapon";
    public static final String SCOPE_KEY  = Ntgl.MOD_ID + ":scope";
    public static final Vec3 GUN_DEFAULT_ORIGIN = new Vec3(8.0, 0.0, 8.0);
    public static final Vec3 ATTACHMENT_DEFAULT_ORIGIN = new Vec3(8.0, 8.0, 8.0);
    public static final Vec3 RED = new Vec3(255.0, 0.0, 0.0);
    public static final String IRON_SIGHT = "ironSight";
    public static final String CAMERA = "camera";
    public static final String ORIGIN = "origin";

    public static void resetCache() {
        ObjectCache.getInstance(CACHE_KEY).reset();
    }

    private static DataObject getCustomData(ItemStack stack) {
        // First try to get data from attachment data
        if (stack.getItem() instanceof IMeta) {
            return MetaLoader.getInstance().getData(stack.getItem());
        }
        // Otherwise try to get the data from the model
        return FrameworkClientAPI.getOpenModelData(stack, null, null, 0);
    }

    public static Vec3 getScopeCamera(ItemStack stack) {
        // Retrieve position from the model's data
        DataObject customObject = PropertyHelper.getCustomData(stack);
        if (customObject.has(SCOPE_KEY, DataType.OBJECT)) {
            DataObject scopeObject = customObject.getDataObject(SCOPE_KEY);
            if (scopeObject.has(CAMERA, DataType.ARRAY)) {
                DataArray cameraArray = scopeObject.getDataArray(CAMERA);
                return arrayToVec3(cameraArray, Vec3.ZERO);
            }
        }

//        // Old method of getting the camera position
//        if (stack.getItem() instanceof IScope scope) {
//            Scope properties = scope.getProperties();
//            return new Vec3(0, properties.getReticleOffset(), (properties.getViewFinderDistance()) * 16.0).add(ATTACHMENT_DEFAULT_ORIGIN); // 0.72 is magic number I decided to add long ago. Here for backwards compat.
//        }

        return ATTACHMENT_DEFAULT_ORIGIN;
    }

    public static Vec3 getIronSightCamera(ItemStack stack, WeaponConfig modifiedWeaponConfig) {
        var ironSightObject = getObjectByPath(stack, WEAPON_KEY, IRON_SIGHT);
        if (ironSightObject.has(CAMERA, DataType.ARRAY)) {
            DataArray cameraArray = ironSightObject.getDataArray(CAMERA);
            return arrayToVec3(cameraArray, Vec3.ZERO);
        }
        var data = AimingHandler.get().getWeaponData();
        if(data.weapon.getItem() instanceof IWeapon) {
            var zoom = WeaponModifierHelper.getSightOffset(data);
            var cameraX = zoom.x();
            var cameraY = zoom.y();
            var cameraZ = zoom.z();

            var attachment = WeaponStateHelper.getAttachmentItem(AttachmentType.SCOPE, stack);
            if (!attachment.isEmpty()) {
                var scope = (ScopeItem) attachment.getItem();
                var attachmentConfig = modifiedWeaponConfig.findAttachment(scope);
                cameraX += attachmentConfig.getOffset().x;
                cameraY += attachmentConfig.getOffset().y;
                cameraZ += attachmentConfig.getOffset().z;
            }

            return new Vec3(cameraX, cameraY, cameraZ);
        }
        return Vec3.ZERO;
    }

    public static boolean isLegacyIronSight(ItemStack stack) {
        var ironSightObject = getObjectByPath(stack, WEAPON_KEY, IRON_SIGHT);
        return !ironSightObject.has(CAMERA, DataType.ARRAY);
    }

    public static Vec3 getModelOrigin(ItemStack stack, Vec3 defaultOrigin) {
        // Retrieve position from the model's data
        var customObject = PropertyHelper.getCustomData(stack);
        if (customObject.has(MODEL_KEY, DataType.OBJECT)) {
            var modelObject = customObject.getDataObject(MODEL_KEY);
            if (modelObject.has(ORIGIN, DataType.ARRAY)) {
                var originArray = modelObject.getDataArray(ORIGIN);
                return arrayToVec3(originArray, defaultOrigin);
            }
        }
        return defaultOrigin;
    }

    public static int getReticleColor(ItemStack stack) {
        // Prioritise getting the reticle colour from the ItemStack tag
        var tag = stack.getTag();
        if (tag != null && tag.contains("ReticleColor", Tag.TAG_INT)) {
            return tag.getInt("ReticleColor");
        }

        // Attempt to get the colour from the item's meta
        var isScope = stack.getItem() instanceof IAttachment<?> attachment && attachment.getType() == AttachmentType.SCOPE;
        var dataObject = isScope ? getObjectByPath(stack, SCOPE_KEY) : getObjectByPath(stack, WEAPON_KEY, IRON_SIGHT);
        if (dataObject.has("reticleColor", DataType.NUMBER)) {
            return dataObject.getDataNumber("reticleColor").asInt();
        } else if (dataObject.has("reticleColor", DataType.ARRAY)) {
            DataArray array = dataObject.getDataArray("reticleColor");
            Vec3 color = arrayToVec3(array, RED);
            int a = 255;
            int r = Mth.clamp((int) color.x, 0, 255);
            int g = Mth.clamp((int) color.y, 0, 255);
            int b = Mth.clamp((int) color.z, 0, 255);
            return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
        }

        // Default is red
        return 0xFFFF0000;
    }

    public static SightAnimation getSightAnimations(ItemStack weapon) {
        // Try and get the animations from the scope
        if (WeaponStateHelper.hasAttachmentEquipped(weapon, AttachmentType.SCOPE)) {
            var scopeStack = WeaponStateHelper.getScopeStack(weapon);
            if (scopeStack.getItem() instanceof IAttachment<?> attachment && attachment.getType() == AttachmentType.SCOPE) {
                DataObject scopeObject = getObjectByPath(scopeStack, SCOPE_KEY);
                if (scopeObject.get("sightAnimation") instanceof DataObject sightObject) {
                    return objectToSightAnimation(sightObject);
                }
            }
        }

        // Try and get the animations from the weapon
        DataObject customObject = getObjectByPath(weapon, WEAPON_KEY, IRON_SIGHT);
        if (customObject.get("sightAnimation") instanceof DataObject sightObject) {
            return objectToSightAnimation(sightObject);
        }

        return SightAnimation.DEFAULT;
    }

    public static double getViewportFov(ItemStack weapon) {
        // Get the viewport from the attached scope
        if (WeaponStateHelper.hasAttachmentEquipped(weapon, AttachmentType.SCOPE)) {
            var scopeStack = WeaponStateHelper.getScopeStack(weapon);
            var customObject = getObjectByPath(scopeStack, SCOPE_KEY);
            if (customObject.has("viewportFov", DataType.NUMBER)) {
                return Mth.clamp(customObject.getDataNumber("viewportFov").asDouble(), 1.0, 100.0);
            }
        }

        // Otherwise get it from the weapon
        DataObject customObject = getObjectByPath(weapon, WEAPON_KEY, IRON_SIGHT);
        if (customObject.has("viewportFov", DataType.NUMBER)) {
            return Mth.clamp(customObject.getDataNumber("viewportFov").asDouble(), 1.0, 100.0);
        }

        // Return zero, which means current fov is used
        return 0;
    }

    private static SightAnimation objectToSightAnimation(DataObject object) {
        ObjectCache cache = ObjectCache.getInstance(CACHE_KEY);
        Optional<SightAnimation> cachedValue = cache.get(object.getId());
        return cachedValue.orElseGet(() -> cache.store(object.getId(), () -> {
            SightAnimation.Builder builder = SightAnimation.builder();
            getOptionalString(object, "viewportCurve").ifPresent(s -> builder.setViewportCurve(Easings.byName(s)));
            getOptionalString(object, "sightCurve").ifPresent(s -> builder.setSightCurve(Easings.byName(s)));
            getOptionalString(object, "fovCurve").ifPresent(s -> builder.setFovCurve(Easings.byName(s)));
            getOptionalString(object, "aimTransformCurve").ifPresent(s -> builder.setAimTransformCurve(Easings.byName(s)));
            return builder.build();
        }));
    }

    private static Optional<String> getOptionalString(DataObject src, String key) {
        if (src.has(key, DataType.STRING)) {
            return Optional.ofNullable(src.getDataString(key).asString());
        }
        return Optional.empty();
    }

    private static DataObject getObjectByPath(ItemStack stack, String... path) {
        DataObject result = PropertyHelper.getCustomData(stack);
        for (String key : path) {
            if (result.has(key, DataType.OBJECT)) {
                result = result.getDataObject(key);
                continue;
            }
            return DataObject.EMPTY;
        }
        return result;
    }

    private static Vec3 arrayToVec3(DataArray array, Vec3 defaultValue) {
        // Ignore immediately if not correct length
        if (array.length() != 3)
            return defaultValue;

        // Return cached vector, otherwise convert array and cache the vector
        ObjectCache cache = ObjectCache.getInstance(CACHE_KEY);
        Optional<Vec3> cachedValue = cache.get(array.getId());
        return cachedValue.orElseGet(() -> cache.store(array.getId(), () ->
        {
            if (array.values().stream().allMatch(entry -> entry.getType() == DataType.NUMBER)) {
                double x = ((DataNumber) array.get(0)).asDouble();
                double y = ((DataNumber) array.get(1)).asDouble();
                double z = ((DataNumber) array.get(2)).asDouble();
                return new Vec3(x, y, z);
            }
            return defaultValue;
        }));


    }
}

package com.nukateam.chassis_core.client.render.utils;

import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class ResourceHelper {
    public static String getResourceName(ResourceLocation resourceLocation) {
        var path = resourceLocation.getPath();
        return removeExtension(getName(path));
    }

    public static ResourceLocation getChassisResource(String path, String extension) {
        var chassis = PlayerUtils.getLocalPlayerChassis();
        return getChassisResource(chassis, path, extension);
    }

    public static ResourceLocation getChassisResource(Chassis chassis, String path, String extension) {
        var name = chassis.getModelId();
        var modId = ForgeRegistries.ENTITY_TYPES.getKey(chassis.getType()).getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }
}

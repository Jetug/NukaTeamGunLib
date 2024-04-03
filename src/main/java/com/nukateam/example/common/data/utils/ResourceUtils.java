package com.nukateam.example.common.data.utils;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

public class ResourceUtils {
    public static ResourceLocation modResource(String path) {
        return new ResourceLocation(Ntgl.MOD_ID, path);
    }

    public static String getResourceName(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return FilenameUtils.removeExtension(FilenameUtils.getName(path));
    }
}

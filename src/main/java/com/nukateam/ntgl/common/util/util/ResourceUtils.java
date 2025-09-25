package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class ResourceUtils {
    public static ResourceLocation modResource(String path) {
        return ResourceLocation.tryBuild(Ntgl.MOD_ID, path);
    }

    public static String getResourceName(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return FilenameUtils.removeExtension(FilenameUtils.getName(path));
    }

//    public static boolean resourceExists(ResourceLocation path) {
//        var minecraft = Minecraft.getInstance();
//        var buff = minecraft.getResourceManager().listResources("textures", rl -> rl.equals(path));
//        return !buff.isEmpty();
//    }

    public static boolean resourceExists(ResourceLocation path) {
        var minecraft = Minecraft.getInstance();
        var resource = minecraft.getResourceManager().getResource(path);
        return resource.isPresent();
    }
}

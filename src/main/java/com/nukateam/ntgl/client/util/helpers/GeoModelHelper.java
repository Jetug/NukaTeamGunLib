package com.nukateam.ntgl.client.util.helpers;

import com.nukateam.geo.interfaces.IResourceProvider;
import net.minecraft.resources.ResourceLocation;

public class GeoModelHelper {
    public static ResourceLocation getGunResource(IResourceProvider animator, String path, String extension) {
        var name  = animator.getId().getPath();
        var modId = animator.getId().getNamespace();

        return ResourceLocation.tryBuild(modId, path + name + extension);
    }
}

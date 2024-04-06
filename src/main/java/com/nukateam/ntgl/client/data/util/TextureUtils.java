package com.nukateam.ntgl.client.data.util;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

public class TextureUtils {
    public static Pair<Integer, Integer> getTextureSize(ResourceLocation resourceLocation) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).get();
            NativeImage nativeImage = NativeImage.read(resource.open());
            return Pair.of(nativeImage.getWidth(), nativeImage.getHeight());
        } catch (IOException var3) {
            return Pair.of(0, 0);
        }
    }
}

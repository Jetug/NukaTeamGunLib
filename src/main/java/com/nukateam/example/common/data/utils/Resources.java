package com.nukateam.example.common.data.utils;

import com.nukateam.gunscore.GunMod;
import net.minecraft.resources.ResourceLocation;

public class Resources {
    public static ResourceLocation nukaResource(String path) {
        return new ResourceLocation(GunMod.MOD_ID, path);
    }
}

package com.nukateam.ntgl.modules.datapack;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple wrapper for a gun object to pass to GunItem. This is to indicate to developers that
 * Gun instances shouldn't be changed on GunItems as they are controlled by NetworkGunManager.
 * Changes to gun properties should be made through the JSON file.
 */
public class ConfigSupplier<S extends INBTSerializable<CompoundTag>> {
    private final S config;

    public ConfigSupplier(S config) {
        this.config = config;
    }

    public S getConfig() {
        return this.config;
    }
}

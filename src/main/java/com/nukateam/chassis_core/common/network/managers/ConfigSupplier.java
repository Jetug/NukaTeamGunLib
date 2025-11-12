package com.nukateam.chassis_core.common.network.managers;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple wrapper for a config object to pass to item. This is to indicate to developers that
 * Config instances shouldn't be changed on items with config as they are controlled by NetworkWeaponManager.
 * Changes to gun properties should be made through the JSON file.
 */
public class ConfigSupplier<S extends INBTSerializable<CompoundTag>> {
    private final S config;

    ConfigSupplier(S config) {
        this.config = config;
    }

    public S getConfig() {
        return this.config;
    }
}

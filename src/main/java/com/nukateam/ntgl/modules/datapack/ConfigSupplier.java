package com.nukateam.ntgl.modules.datapack;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * A simple wrapper for a gun object to pass to WeaponItem. This is to indicate to developers that
 * Gun instances shouldn't be changed on GunItems as they are controlled by NetworkWeaponManager.
 * Changes to gun properties should be made through the JSON file.
 */
public record ConfigSupplier<S extends INBTSerializable<CompoundTag>>(S config) { }

package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.base.NetworkAmmoManager;
import com.nukateam.ntgl.common.base.NetworkManager;
import com.nukateam.ntgl.common.base.config.Ammo;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple interface to indicate that this item is ammo. This will make sure that it's put into the
 * correct category in the workbench.
 * <p>
 * Author: MrCrayfish
 */
public interface IAmmo <T extends INBTSerializable<CompoundTag>> {
    Ammo getAmmo();

    default IGunModifier[] getModifiers() {
        return new IGunModifier[0];
    }

    void setConfig(NetworkManager.Supplier<T> supplier);
}

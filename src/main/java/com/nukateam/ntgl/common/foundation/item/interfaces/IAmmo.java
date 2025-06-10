package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.IConfigConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple interface to indicate that this item is ammo. This will make sure that it's put into the
 * correct category in the workbench.
 * <p>
 * Author: MrCrayfish
 */
public interface IAmmo <T extends INBTSerializable<CompoundTag>> extends IConfigConsumer<T> {
    Ammo getConfig();

    default IGunModifier[] getModifiers() {
        return new IGunModifier[0];
    }
}

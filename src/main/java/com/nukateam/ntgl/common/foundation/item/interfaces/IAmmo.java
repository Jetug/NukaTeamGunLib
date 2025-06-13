package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.Projectile;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.IConfigConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A simple interface to indicate that this item is projectile. This will make sure that it's put into the
 * correct category in the workbench.
 * <p>
 * Author: MrCrayfish
 */
public interface IAmmo <T extends INBTSerializable<CompoundTag>> extends IConfigConsumer<T> {
    Projectile getAmmo();

    default IGunModifier[] getModifiers() {
        return new IGunModifier[0];
    }
}

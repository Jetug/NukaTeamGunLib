package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IConfigConsumer<T extends INBTSerializable<CompoundTag>> {
    void setConfig(ConfigSupplier<T> supplier);
}

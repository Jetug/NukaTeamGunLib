package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.base.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IConfigConsumer<T extends INBTSerializable<CompoundTag>> {
    void setConfig(NetworkManager.Supplier<T> supplier);
}

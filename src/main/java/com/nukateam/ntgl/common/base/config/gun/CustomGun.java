package com.nukateam.ntgl.common.base.config.gun;

import com.nukateam.ntgl.common.base.config.gun.Gun;
import com.nukateam.ntgl.common.data.annotation.Ignored;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
public class CustomGun implements INBTSerializable<CompoundTag> {
    @Ignored
    public ItemStack model;
    public Gun gun;

    public ItemStack getModel() {
        return this.model;
    }

    public Gun getGun() {
        return this.gun;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", this.model.save(new CompoundTag()));
        compound.put("Gun", this.gun.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.model = ItemStack.of(compound.getCompound("Model"));
        var key = ForgeRegistries.ITEMS.getKey(model.getItem());
        this.gun = Gun.create(key, compound.getCompound("Gun"));
    }
}

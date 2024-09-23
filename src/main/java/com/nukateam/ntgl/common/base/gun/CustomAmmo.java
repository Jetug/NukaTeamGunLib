package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.common.data.annotation.Ignored;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Author: MrCrayfish
 */
public class CustomAmmo implements INBTSerializable<CompoundTag> {
    @Ignored
    public ItemStack model;
    public Ammo ammo;

    public ItemStack getModel() {
        return this.model;
    }

    public Ammo getAmmo() {
        return this.ammo;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", this.model.save(new CompoundTag()));
        compound.put("Ammo", this.ammo.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.model = ItemStack.of(compound.getCompound("Model"));
        this.ammo = Ammo.create(compound.getCompound("Ammo"));
    }
}

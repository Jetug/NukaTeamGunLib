package com.nukateam.ntgl.common.data.config;

import com.nukateam.ntgl.common.util.annotation.Ignored;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Author: MrCrayfish
 */
public class CustomAmmo implements INBTSerializable<CompoundTag> {
    @Ignored
    public ItemStack model;
    public Ammo projectile;

    public ItemStack getModel() {
        return this.model;
    }

    public Ammo getAmmo() {
        return this.projectile;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", this.model.save(new CompoundTag()));
        compound.put("Projectile", this.projectile.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.model = ItemStack.of(compound.getCompound("Model"));
        this.projectile = Ammo.create(compound.getCompound("Projectile"));
    }
}

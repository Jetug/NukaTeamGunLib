package com.nukateam.ntgl.common.base;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class DynamicGunModifier implements IGunModifier {
    protected LivingEntity entity;
    protected ItemStack stack;

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}

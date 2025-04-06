package com.nukateam.ntgl.common.base;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class DynamicGunModifier implements IGunModifier {
    protected LivingEntity entity;
    protected ItemStack stack;
    @Nullable
    protected HumanoidArm arm;

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    public DynamicGunModifier setEntity(LivingEntity entity) {
        this.entity = entity;
        return this;
    }

    public ItemStack getStack() {
        return stack;
    }

    public DynamicGunModifier setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public DynamicGunModifier setArm(@Nullable HumanoidArm arm) {
        this.arm = arm;
        return this;
    }

    public @Nullable HumanoidArm getArm() {
        return arm;
    }
}

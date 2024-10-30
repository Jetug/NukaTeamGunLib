package com.nukateam.ntgl.common.foundation.entity.misc;

import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AshPile extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    public static final int LIFE = 75;
    public int timeToLive = LIFE;

    public AshPile(EntityType<?> entityType, Level pLevel) {
        super(entityType, pLevel);
    }

    public AshPile(Level pLevel, Vec3 pos) {
        super(ModEntityTypes.ASH_PILE.get(), pLevel);
        this.setPos(pos);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.timeToLive > 0)
            --timeToLive;
        else this.kill();
    }

    public int getMaxLife(){
        return LIFE;
    }

    public int getLife(){
        return timeToLive;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

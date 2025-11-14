package com.nukateam.ntgl.common.foundation.entity.misc;

import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AshPile extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final int LIFE = 20 * 10;
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

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

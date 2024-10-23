package com.nukateam.example.common.entities;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.nukateam.example.common.registery.EntityTypes.BRAHMIN;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

public class Brahmin extends Cow implements GeoEntity, Shearable, IForgeShearable {
    private static final EntityDataAccessor<Boolean> HAS_BALLS = SynchedEntityData.defineId(Brahmin.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    private boolean partyBrahmin;

    public Brahmin(EntityType<? extends Cow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HAS_BALLS, true);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("hasBalls", this.hasBalls());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.hasBalls(compound.getBoolean("hasBalls"));
    }

    @Nullable
    @Override
    public Brahmin getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return BRAHMIN.get().create(level);
    }

    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return readyForShearing();
    }

    @Override
    public boolean readyForShearing() {
        return hasBalls();
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level, BlockPos pos, int fortune) {
        var soundSource = player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS;
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1.0F, 1.0F);
        this.gameEvent(GameEvent.SHEAR, player);


        return Collections.emptyList();
    }

    @Override
    public void shear(SoundSource pSource) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, pSource, 1.0F, 1.0F);
        this.hasBalls(false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, event -> {
            var controller = event.getController();
            controller.setAnimationSpeed(1);
            var animation = begin();

            if(isPartyBrahmin() && !hasBalls()){
                animation.thenLoop("dance");
            }
            else if (event.isMoving()) {
                animation.thenLoop("walk");
            }

            else return PlayState.STOP;

            return event.setAndContinue(animation);
        }));
    }

    @Override
    public void setRecordPlayingNearby(BlockPos pPos, boolean pIsPartying) {
        this.partyBrahmin = pIsPartying;
    }

    @Override
    public boolean canBreed() {
        return hasBalls();
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && hasBalls();
    }

    public boolean isPartyBrahmin() {
        return this.partyBrahmin;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean hasBalls() {
        return this.entityData.get(HAS_BALLS);
    }

    public void hasBalls(boolean value) {
        this.entityData.set(HAS_BALLS, value);
    }

    private void spawnItemEntity(ItemStack stack) {
        var itemEntity = this.spawnAtLocation(stack);

        if (itemEntity != null) {
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add((
                            this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                    this.random.nextFloat() * 0.05F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
    }
}

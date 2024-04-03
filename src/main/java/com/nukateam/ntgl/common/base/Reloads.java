//package com.dayton.guns.common.base;
//
//import com.dayton.guns.Config;
//import com.dayton.guns.common.data.annotation.Optional;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraftforge.common.util.INBTSerializable;
//import net.minecraftforge.fml.util.thread.SidedThreadGroups;
//
//public static class Reloads implements INBTSerializable<CompoundTag>
//{
//    private int maxAmmo = 20;
//    @Optional
//    private boolean magFed = false;
//    @Optional
//    private int reloadMagTimer = 20;
//    @Optional
//    private int additionalReloadEmptyMagTimer = 0;
//    @Optional
//    private int reloadAmount = 1;
//    @Optional
//    private int[] maxAdditionalAmmoPerOC = new int[]{};
//    @Optional
//    private int preReloadPauseTicks = 0;
//    @Optional
//    private int interReloadPauseTicks = 1;
//    @Optional
//    private boolean openBolt = false;
//
//    @Override
//    public CompoundTag serializeNBT()
//    {
//        CompoundTag tag = new CompoundTag();
//        tag.putInt("MaxAmmo", this.maxAmmo);
//        tag.putBoolean("MagFed", this.magFed);
//        tag.putInt("ReloadSpeed", this.reloadAmount);
//        tag.putInt("ReloadMagTimer", this.reloadMagTimer);
//        tag.putInt("AdditionalReloadEmptyMagTimer", this.additionalReloadEmptyMagTimer);
//        tag.putIntArray("MaxAmmunitionPerOverCap", this.maxAdditionalAmmoPerOC);
//        tag.putInt("ReloadPauseTicks", this.preReloadPauseTicks);
//        tag.putInt("InterReloadPauseTicks", this.interReloadPauseTicks);
//        tag.putBoolean("OpenBolt", this.openBolt);
//        return tag;
//    }
//
//    @Override
//    public void deserializeNBT(CompoundTag tag)
//    {
//        if(tag.contains("MaxAmmo", Tag.TAG_ANY_NUMERIC))
//        {
//            this.maxAmmo = tag.getInt("MaxAmmo");
//        }
//        if(tag.contains("MagFed", Tag.TAG_ANY_NUMERIC))
//        {
//            this.magFed = tag.getBoolean("MagFed");
//        }
//        if(tag.contains("ReloadSpeed", Tag.TAG_ANY_NUMERIC))
//        {
//            this.reloadAmount = tag.getInt("ReloadSpeed");
//        }
//        if(tag.contains("ReloadMagTimer", Tag.TAG_ANY_NUMERIC))
//        {
//            this.reloadMagTimer = tag.getInt("ReloadMagTimer");
//        }
//        if(tag.contains("AdditionalReloadEmptyMagTimer", Tag.TAG_ANY_NUMERIC))
//        {
//            this.additionalReloadEmptyMagTimer = tag.getInt("AdditionalReloadEmptyMagTimer");
//        }
//        if(tag.contains("MaxAmmunitionPerOverCap", Tag.TAG_INT_ARRAY))
//        {
//            this.maxAdditionalAmmoPerOC = tag.getIntArray("MaxAmmunitionPerOverCap");
//        }
//        if(tag.contains("ReloadPauseTicks", Tag.TAG_ANY_NUMERIC))
//        {
//            this.preReloadPauseTicks = tag.getInt("ReloadPauseTicks");
//        }
//        if(tag.contains("InterReloadPauseTicks", Tag.TAG_ANY_NUMERIC))
//        {
//            this.interReloadPauseTicks = tag.getInt("InterReloadPauseTicks");
//        }
//        if(tag.contains("OpenBolt", Tag.TAG_ANY_NUMERIC))
//        {
//            this.openBolt = tag.getBoolean("OpenBolt");
//        }
//    }
//
//    /**
//     * @return A copy of the general get
//     */
//    public Reloads copy()
//    {
//        Reloads reloads = new Reloads();
//        reloads.magFed = this.magFed;
//        reloads.maxAmmo = this.maxAmmo;
//        reloads.reloadAmount = this.reloadAmount;
//        reloads.reloadMagTimer = this.reloadMagTimer;
//        reloads.additionalReloadEmptyMagTimer = this.additionalReloadEmptyMagTimer;
//        reloads.maxAdditionalAmmoPerOC = this.maxAdditionalAmmoPerOC;
//        reloads.preReloadPauseTicks = this.preReloadPauseTicks;
//        reloads.interReloadPauseTicks = this.interReloadPauseTicks;
//        reloads.openBolt = this.openBolt;
//        return reloads;
//    }
//
//    /**
//     * @return Does this gun reload all ammunition following a single timer and replenish
//     */
//    public boolean isMagFed() {return this.magFed;}
//    /**
//     * @return The maximum amount of ammo this weapon can hold
//     */
//    public int getMaxAmmo()
//    {
//        return this.maxAmmo;
//    }
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int getReloadAmount() {return (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER
//            && Config.COMMON.development.enableTDev.get()
//            && GunEditor.get().getMode() == GunEditor.TaCWeaponDevModes.reloads) ?
//            (int) (this.reloadAmount + GunEditor.get().getReloadAmountMod()) : this.reloadAmount;}
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int getReloadMagTimer()
//    {
//        return (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER
//                && GunEditor.get().getMode() == GunEditor.TaCWeaponDevModes.reloads) ?
//                (int) (this.reloadMagTimer + GunEditor.get().getReloadMagTimerMod()) : this.reloadMagTimer;
//    }
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int getAdditionalReloadEmptyMagTimer()
//    {
//        return (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER &&
//                Config.COMMON.development.enableTDev.get() &&
//                GunEditor.get().getMode() == GunEditor.TaCWeaponDevModes.reloads) ?
//                (int) (this.additionalReloadEmptyMagTimer + GunEditor.get().getAdditionalReloadEmptyMagTimerMod()) :
//                this.additionalReloadEmptyMagTimer;
//    }
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int[] getMaxAdditionalAmmoPerOC() {return this.maxAdditionalAmmoPerOC;}
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int getPreReloadPauseTicks()
//    {
//        return (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER && Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER && Config.COMMON.development.enableTDev.get() && GunEditor.get().getMode() == GunEditor.TaCWeaponDevModes.reloads) ? (int) (this.preReloadPauseTicks + GunEditor.get().getPreReloadPauseTicksMod()) : this.preReloadPauseTicks;
//    }
//    /**
//     * @return The amount of ammo to add to the weapon each reload cycle
//     */
//    public int getinterReloadPauseTicks() {return (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER && Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER && Config.COMMON.development.enableTDev.get() && GunEditor.get().getMode() == GunEditor.TaCWeaponDevModes.reloads) ? (int) (this.interReloadPauseTicks + GunEditor.get().getInterReloadPauseTicksMod()) : this.interReloadPauseTicks;}
//    /**
//     * @return Does this gun reload all ammunition following a single timer and replenish
//     */
//    public boolean isOpenBolt()
//    {
//        return this.openBolt;
//    }
//}

package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.*;
import com.nukateam.ntgl.common.debug.*;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class WeaponSettings implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String GENERAL = "General";
    public static final String MELEE = "Melee";
    public static final String THROWABLE = "Throwable";
    public static final String AMMO_DATA = "AmmoData";
    public static final String SECONDARY_AMMO = "SecondaryAmmo";
    public static final String ZOOM = "Zoom";
    protected General general = new General();
    protected Melee melee = new Melee();
    protected ThrowableConfig throwable = new ThrowableConfig();
    protected LinkedHashMap<ResourceLocation, AmmoData> ammoData = new LinkedHashMap<>();
    protected LinkedHashMap<ResourceLocation, Fuel> fuel = new LinkedHashMap<>();
    @Optional @Nullable protected Zoom zoom;

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {}

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(GENERAL, this.general.serializeNBT());
        tag.put(MELEE, this.melee.serializeNBT());
        tag.put(THROWABLE, this.throwable.serializeNBT());
        tag.put(AMMO_DATA, NbtUtils.serializeMap(this.ammoData));
        tag.put(SECONDARY_AMMO, NbtUtils.serializeMap(this.fuel));
        if (this.zoom != null) {
            tag.put(ZOOM, this.zoom.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(GENERAL, Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound(GENERAL));
        }
        if (tag.contains(MELEE, Tag.TAG_COMPOUND)) {
            this.melee.deserializeNBT(tag.getCompound(MELEE));
        }
        if (tag.contains(THROWABLE, Tag.TAG_COMPOUND)) {
            this.throwable.deserializeNBT(tag.getCompound(THROWABLE));
        }
        if (tag.contains(AMMO_DATA, Tag.TAG_COMPOUND)) {
            this.ammoData = NbtUtils.deserializeLinkedMap(tag.getCompound(AMMO_DATA), (nbt) -> AmmoData.create(nbt));
        }
        if (tag.contains(SECONDARY_AMMO, Tag.TAG_COMPOUND)) {
            this.fuel = NbtUtils.deserializeLinkedMap(tag.getCompound(SECONDARY_AMMO), (nbt) -> Fuel.create(nbt));
        }
        if(tag.contains(ZOOM, Tag.TAG_COMPOUND)) {
            this.zoom = Zoom.create(tag.getCompound(ZOOM));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("melee", this.melee.toJsonObject());
        object.add("throwable", this.throwable.toJsonObject());
        if (this.zoom != null)
            object.add("zoom", this.zoom.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"ammoData", gson.toJsonTree(this.ammoData).getAsJsonObject());
        return object;
    }

    public WeaponSettings copy() {
        var gun = new WeaponSettings();
        gun.general = this.general.copy();
        gun.melee = this.melee.copy();
        gun.throwable = this.throwable.copy();
        gun.zoom = this.zoom.copy();
        gun.ammoData = (LinkedHashMap<ResourceLocation, AmmoData>) this.ammoData.clone();
        gun.fuel = (LinkedHashMap<ResourceLocation, Fuel>) this.fuel.clone();
        return gun;
    }

    public static WeaponSettings create(CompoundTag tag) {
        var gun = new WeaponSettings();
        gun.deserializeNBT(tag);
        return gun;
    }

    public General getGeneral() {
        return this.general;
    }

    public Melee getMelee() {
        return this.melee;
    }

    public ThrowableConfig getThrowable() {
        return this.throwable;
    }

    public AmmoData getAmmoData(ResourceLocation ammo) {
        return ammoData.getOrDefault(ammo, new AmmoData());
    }

    public Fuel getFuelData(ResourceLocation ammo) {
        return fuel.getOrDefault(ammo, new Fuel());
    }

    public boolean hasAmmo(ResourceLocation ammo){
        return ammoData.containsKey(ammo);
    }

    @Nullable
    public Zoom getZoom() {
        return zoom;
    }

    public ProjectileConfig getProjectileConfig(ResourceLocation ammo){
        return getAmmoData(ammo).getProjectile();
    }

    public AmmoConfig getAmmoConfig(ResourceLocation ammo){
        return getAmmoData(ammo).getAmmo();
    }

    public Fuel getFuelConfig(ResourceLocation ammo) {
        return getFuelData(ammo);
    }

    public AmmoConfig getFuelAmmoConfig(ResourceLocation ammo){
        return getFuelData(ammo).getAmmo();
    }

    public static class Builder {
        private final WeaponSettings gun;

        private Builder() {
            this.gun = new WeaponSettings();
        }

        private Builder(WeaponSettings gun) {
            this.gun = gun.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(WeaponSettings gun) {
            return new Builder(gun);
        }

        public WeaponSettings build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public WeaponSettings.Builder addAmmo(AmmoHolder id) {
            this.gun.general.ammo.add(id);
            return this;
        }

        public Builder setFireRate(int rate) {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.gun.general.gripType = gripType;
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo) {
            this.gun.general.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount) {
            this.gun.general.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setReloadTime(int reloadTime) {
            this.gun.general.reloadTime = reloadTime;
            return this;
        }

        public Builder setLoadingType(LoadingType loadingType) {
            this.gun.general.loadingType = loadingType;
            return this;
        }

        public Builder setCategory(String category) {
            this.gun.general.category = category;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle) {
            this.gun.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilKick(float recoilKick) {
            this.gun.general.recoilKick = recoilKick;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset) {
            this.gun.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction) {
            this.gun.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount) {
            this.gun.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread) {
            this.gun.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread) {
            this.gun.general.spread = spread;
            return this;
        }
    }
}

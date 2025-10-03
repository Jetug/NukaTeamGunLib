package com.nukateam.ntgl.common.data.config.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.Ntgl;

import com.nukateam.ntgl.common.data.holders.*;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import com.nukateam.ntgl.common.util.annotation.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class General implements INBTSerializable<CompoundTag> {
    public static final String LOADING_TYPE = "LoadingType";
    public static final String AUTO_RELOAD = "AutoReload";
    public static final String RATE = "Rate";
    public static final String GRIP_TYPE = "GripType";
    public static final String RELOAD_TYPE = "ReloadType";
    public static final String MAX_AMMO = "MaxAmmo";
    public static final String RELOAD_SPEED = "ReloadSpeed";
    public static final String RELOAD_START = "reloadStart";
    public static final String RELOAD_TIME = "ReloadTime";
    public static final String RELOAD_END = "ReloadEnd";
    public static final String RECOIL_ANGLE = "RecoilAngle";
    public static final String DAMAGE = "Damage";
    public static final String RECOIL_KICK = "RecoilKick";
    public static final String RECOIL_DURATION_OFFSET = "RecoilDurationOffset";
    public static final String RECOIL_ADS_REDUCTION = "RecoilAdsReduction";
    public static final String PROJECTILE_AMOUNT = "ProjectileAmount";
    public static final String MULTISHOT_AMOUNT = "multishotAmount";
    public static final String ALWAYS_SPREAD = "AlwaysSpread";
    public static final String SPREAD = "Spread";
    public static final String CATEGORY = "category";
    public static final String MOVEMENT_MODIFIER = "MovementModifier";
    public static final String AMMO = "Ammo";
    public static final String FUEL = "Fuel";
    public static final String FULL_CHARGE = "FullCharge";
    public static final String ENCHANTABLE = "Enchantable";
    public static final String SILENCED = "silenced";
    public static final String FIRE_TIMER = "FireTimer";
    public static final String FIRE_MODE = "FireMode";
    public static final String ONE_TIME_CHARGE = "OneTimeCharge";
    public static final String MELEE = "melee";
    public static final String EQUIP_TIME = "EquipTime";
    public static final String AMMO_PER_SHOT = "AmmoPerShot";
    public static final String RENDER_HUD = "RenderHud";
    public static final String WEAPON_MODE = "WeaponMode";

    int rate;
    int maxAmmo;
    @Optional LinkedHashSet<FireMode> fireMode = new LinkedHashSet<>(List.of(FireMode.SEMI_AUTO));
    @Optional WeaponMode weaponMode = WeaponMode.GUN;
    @Optional boolean fullCharge = false;
    @Optional boolean enchantable = true;
    @Optional boolean silenced = false;
    @Optional float damage;
    @Optional int reloadAmount = 1;
    @Optional int reloadStart = 0;
    @Optional int reloadTime = 1;
    @Optional int reloadEnd = 0;
    @Optional int equipTime = 1;
    @Optional int ammoPerShot = 1;
    @Ignored GripType gripType = GripType.ONE_HANDED;
    @Optional LoadingType loadingType = LoadingType.MAGAZINE;
    @Optional String category = "pistol";
    @Optional boolean autoReload = false;
    @Optional boolean renderHud = true;
    @Optional float recoilAngle;
    @Optional float recoilKick;
    @Optional float recoilDurationOffset;
    @Optional float recoilAdsReduction = 0.2F;
    @Optional int projectileAmount = 1;
    @Optional int multishotAmount = 2;
    @Optional boolean alwaysSpread;
    @Optional boolean oneTimeCharge = true;
    @Optional float spread;
    @Optional int fireTimer;
    @Optional float movementSpeed = 0.0f;
    @Optional boolean melee = false;
    @Optional protected LinkedHashSet<AmmoHolder> ammo = new LinkedHashSet<>(List.of(AmmoHolder.getType(Ntgl.MOD_ID + ":round10mm")));
    @Optional protected LinkedHashSet<AmmoHolder> fuel = new LinkedHashSet<>();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt      (RATE, this.rate);
        tag.putBoolean  (FULL_CHARGE, this.fullCharge);
        tag.putBoolean  (ENCHANTABLE, this.enchantable);
        tag.putBoolean  (SILENCED, this.silenced);
        tag.putInt      (FIRE_TIMER, this.fireTimer);
        tag.put         (FIRE_MODE, NbtUtils.serializeSet(this.fireMode));
        tag.putString   (GRIP_TYPE, this.gripType.getId().toString());
        tag.putInt      (MAX_AMMO, this.maxAmmo);
        tag.putInt      (RELOAD_SPEED, this.reloadAmount);
        tag.putInt      (RELOAD_START, this.reloadStart);
        tag.putInt      (RELOAD_TIME, this.reloadTime);
        tag.putInt      (RELOAD_END, this.reloadEnd);
        tag.putInt      (EQUIP_TIME, this.equipTime);
        tag.putInt      (AMMO_PER_SHOT, this.ammoPerShot);
        tag.putString   (LOADING_TYPE, this.loadingType.toString());
        tag.putString   (WEAPON_MODE, this.weaponMode.toString());
        tag.putBoolean  (AUTO_RELOAD, this.autoReload);
        tag.putBoolean  (RENDER_HUD, this.renderHud);
        tag.putString   (CATEGORY, this.category);
        tag.putFloat    (RECOIL_ANGLE, this.recoilAngle);
        tag.putFloat    (DAMAGE, this.damage);
        tag.putFloat    (RECOIL_KICK, this.recoilKick);
        tag.putFloat    (RECOIL_DURATION_OFFSET, this.recoilDurationOffset);
        tag.putFloat    (RECOIL_ADS_REDUCTION, this.recoilAdsReduction);
        tag.putInt      (PROJECTILE_AMOUNT, this.projectileAmount);
        tag.putInt      (MULTISHOT_AMOUNT, this.multishotAmount);
        tag.putFloat    (SPREAD, this.spread);
        tag.putFloat    (MOVEMENT_MODIFIER, this.movementSpeed);
        tag.putBoolean  (ALWAYS_SPREAD, this.alwaysSpread);
        tag.putBoolean  (ONE_TIME_CHARGE, this.oneTimeCharge);
        tag.putBoolean  (MELEE, this.melee);
        tag.put         (AMMO, NbtUtils.serializeSet(this.ammo));
        tag.put         (FUEL, NbtUtils.serializeSet(this.fuel));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(FIRE_MODE, Tag.TAG_COMPOUND)) {
            this.fireMode = NbtUtils.deserializeFireMode(tag.getCompound(FIRE_MODE));
        }
        if (tag.contains(FULL_CHARGE, Tag.TAG_ANY_NUMERIC)) {
            this.fullCharge = tag.getBoolean(FULL_CHARGE);
        }
        if (tag.contains(ENCHANTABLE, Tag.TAG_ANY_NUMERIC)) {
            this.enchantable = tag.getBoolean(ENCHANTABLE);
        }
        if (tag.contains(SILENCED, Tag.TAG_ANY_NUMERIC)) {
            this.silenced = tag.getBoolean(SILENCED);
        }
        if (tag.contains(RATE, Tag.TAG_ANY_NUMERIC)) {
            this.rate = tag.getInt(RATE);
        }
        if (tag.contains(FIRE_TIMER, Tag.TAG_ANY_NUMERIC)) {
            this.fireTimer = tag.getInt(FIRE_TIMER);
        }
        if (tag.contains(GRIP_TYPE, Tag.TAG_STRING)) {
            this.gripType = GripType.getType(ResourceLocation.tryParse(tag.getString(GRIP_TYPE)));
        }
        if (tag.contains(MAX_AMMO, Tag.TAG_ANY_NUMERIC)) {
            this.maxAmmo = tag.getInt(MAX_AMMO);
        }
        if (tag.contains(RELOAD_SPEED, Tag.TAG_ANY_NUMERIC)) {
            this.reloadAmount = tag.getInt(RELOAD_SPEED);
        }
        if (tag.contains(RELOAD_START, Tag.TAG_ANY_NUMERIC)) {
            this.reloadStart = tag.getInt(RELOAD_START);
        }
        if (tag.contains(RELOAD_TIME, Tag.TAG_ANY_NUMERIC)) {
            this.reloadTime = tag.getInt(RELOAD_TIME);
        }
        if (tag.contains(RELOAD_END, Tag.TAG_ANY_NUMERIC)) {
            this.reloadEnd = tag.getInt(RELOAD_END);
        }
        if (tag.contains(EQUIP_TIME, Tag.TAG_ANY_NUMERIC)) {
            this.equipTime = tag.getInt(EQUIP_TIME);
        }
        if (tag.contains(AMMO_PER_SHOT, Tag.TAG_ANY_NUMERIC)) {
            this.ammoPerShot = tag.getInt(AMMO_PER_SHOT);
        }
        if (tag.contains(LOADING_TYPE, Tag.TAG_STRING)) {
            this.loadingType = LoadingType.getType(tag.getString(LOADING_TYPE));
        }
        if (tag.contains(WEAPON_MODE, Tag.TAG_STRING)) {
            this.weaponMode = WeaponMode.getType(tag.getString(WEAPON_MODE));
        }
        if (tag.contains(AUTO_RELOAD, Tag.TAG_BYTE)) {
            this.autoReload = tag.getBoolean(AUTO_RELOAD);
        }
        if (tag.contains(RENDER_HUD, Tag.TAG_BYTE)) {
            this.renderHud = tag.getBoolean(RENDER_HUD);
        }
        if (tag.contains(CATEGORY, Tag.TAG_STRING)) {
            this.category = tag.getString(CATEGORY);
        }
        if (tag.contains(RECOIL_ANGLE, Tag.TAG_ANY_NUMERIC)) {
            this.recoilAngle = tag.getFloat(RECOIL_ANGLE);
        }
        if (tag.contains(DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat(DAMAGE);
        }
        if (tag.contains(RECOIL_KICK, Tag.TAG_ANY_NUMERIC)) {
            this.recoilKick = tag.getFloat(RECOIL_KICK);
        }
        if (tag.contains(RECOIL_DURATION_OFFSET, Tag.TAG_ANY_NUMERIC)) {
            this.recoilDurationOffset = tag.getFloat(RECOIL_DURATION_OFFSET);
        }
        if (tag.contains(RECOIL_ADS_REDUCTION, Tag.TAG_ANY_NUMERIC)) {
            this.recoilAdsReduction = tag.getFloat(RECOIL_ADS_REDUCTION);
        }
        if (tag.contains(PROJECTILE_AMOUNT, Tag.TAG_ANY_NUMERIC)) {
            this.projectileAmount = tag.getInt(PROJECTILE_AMOUNT);
        }
        if (tag.contains(MULTISHOT_AMOUNT, Tag.TAG_ANY_NUMERIC)) {
            this.multishotAmount = tag.getInt(MULTISHOT_AMOUNT);
        }
        if (tag.contains(ONE_TIME_CHARGE)) {
            this.oneTimeCharge = tag.getBoolean(ONE_TIME_CHARGE);
        }
        if (tag.contains(MELEE)) {
            this.melee = tag.getBoolean(MELEE);
        }
        if (tag.contains(ALWAYS_SPREAD)) {
            this.alwaysSpread = tag.getBoolean(ALWAYS_SPREAD);
        }
        if (tag.contains(SPREAD, Tag.TAG_ANY_NUMERIC)) {
            this.spread = tag.getFloat(SPREAD);
        }
        if (tag.contains(MOVEMENT_MODIFIER, Tag.TAG_ANY_NUMERIC)) {
            this.movementSpeed = tag.getFloat(MOVEMENT_MODIFIER);
        }
        if (tag.contains(AMMO, Tag.TAG_COMPOUND)) {
            this.ammo = NbtUtils.deserializeSet(tag.getCompound(AMMO), AmmoHolder::getType);
        }
        if (tag.contains(FUEL, Tag.TAG_COMPOUND)) {
            this.fuel = NbtUtils.deserializeSet(tag.getCompound(FUEL), AmmoHolder::getType);
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.rate > 0, "Rate must be more than zero");
        Preconditions.checkArgument(this.maxAmmo > 0, "Max projectile must be more than zero");
        Preconditions.checkArgument(this.reloadAmount >= 1, "Reload amount must be more than or equal to zero");
        Preconditions.checkArgument(this.reloadTime >= 1, "Reload time must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilAngle >= 0.0F, "Recoil angle must be more than or equal to zero");
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage angle must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilKick >= 0.0F, "Recoil kick must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilDurationOffset >= 0.0F && this.recoilDurationOffset <= 1.0F, "Recoil duration offset must be between 0.0 and 1.0");
        Preconditions.checkArgument(this.recoilAdsReduction >= 0.0F && this.recoilAdsReduction <= 1.0F, "Recoil ads reduction must be between 0.0 and 1.0");
        Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
        Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");
        Preconditions.checkArgument(this.movementSpeed >= 0.0F, "Spread must be more than or equal to zero");
        JsonObject object = new JsonObject();
        if (this.fullCharge) object.addProperty("fullCharge", true);
        object.addProperty("fullCharge", fullCharge);
        object.addProperty("rate", this.rate);
        if (this.fireTimer != 0) object.addProperty("fireTimer", this.fireTimer);
//            object.addProperty("fireMode", this.fireMode.getId().toString());
        object.addProperty("gripType", this.gripType.toString());
        object.addProperty("loadingType", this.loadingType.toString());
        object.addProperty("weaponMode", this.weaponMode.toString());
        object.addProperty("autoReload", this.autoReload);
        object.addProperty("renderHud", this.renderHud);
        object.addProperty("maxAmmo", this.maxAmmo);
        if (this.reloadAmount != 1) object.addProperty("reloadAmount", this.reloadAmount);
        if (this.reloadStart > 0 ) object.addProperty("reloadStart", this.reloadStart);
        if (this.reloadTime != 1) object.addProperty("reloadTime", this.reloadTime);
        if (this.reloadEnd > 0 ) object.addProperty("reloadEnd", this.reloadEnd);
        if (this.equipTime > 0 ) object.addProperty("equipTime", this.equipTime);
        if (this.ammoPerShot > 0 ) object.addProperty("ammoPerShot", this.ammoPerShot);
        if (this.recoilAngle != 0.0F) object.addProperty("recoilAngle", this.recoilAngle);
        if (this.damage != 0.0F) object.addProperty("damage", this.damage);
        if (this.recoilKick != 0.0F) object.addProperty("recoilKick", this.recoilKick);
        if (this.recoilDurationOffset != 0.0F)
            object.addProperty("recoilDurationOffset", this.recoilDurationOffset);
        if (this.recoilAdsReduction != 0.2F) object.addProperty("recoilAdsReduction", this.recoilAdsReduction);
        if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
        object.addProperty("multishotAmount", this.multishotAmount);
        object.addProperty("alwaysSpread", this.alwaysSpread);
        object.addProperty("oneTimeCharge", this.oneTimeCharge);
        object.addProperty("melee", this.melee);
        if (this.movementSpeed != 1.0F) object.addProperty("movementSpeed", true);
        if (this.spread != 0.0F) object.addProperty("spread", this.spread);
//            object.add("", new JsonArray());
        return object;
    }

    /**
     * @return A copy of the general get
     */
    public General copy() {
        General general = new General();
        general.fireMode = this.fireMode;
        general.fullCharge = this.fullCharge;
        general.enchantable = this.enchantable;
        general.silenced = this.silenced;
        general.rate = this.rate;
        general.fireTimer = this.fireTimer;
        general.gripType = this.gripType;
        general.maxAmmo = this.maxAmmo;
        general.reloadAmount = this.reloadAmount;
        general.reloadStart = this.reloadStart;
        general.reloadTime = this.reloadTime;
        general.reloadEnd = this.reloadEnd;
        general.equipTime = this.equipTime;
        general.ammoPerShot = this.ammoPerShot;
        general.loadingType = this.loadingType;
        general.weaponMode = this.weaponMode;
        general.autoReload = this.autoReload;
        general.renderHud = this.renderHud;
        general.category = this.category;
        general.recoilAngle = this.recoilAngle;
        general.damage = this.damage;
        general.recoilKick = this.recoilKick;
        general.recoilDurationOffset = this.recoilDurationOffset;
        general.recoilAdsReduction = this.recoilAdsReduction;
        general.projectileAmount = this.projectileAmount;
        general.multishotAmount = this.multishotAmount;
        general.alwaysSpread = this.alwaysSpread;
        general.spread = this.spread;
        general.oneTimeCharge = this.oneTimeCharge;
        general.melee = this.melee;
        general.movementSpeed = this.movementSpeed;
        general.ammo = new LinkedHashSet<>(this.ammo);
        general.fuel = new LinkedHashSet<>(this.fuel);
        return general;
    }

    public static General create(CompoundTag tag) {
        var general = new General();
        general.deserializeNBT(tag);
        return general;
    }

    public Set<AmmoHolder> getAmmo() {
        return this.ammo;
    }

    public Set<AmmoHolder> getFuel() {
        return this.fuel;
    }

    /**
     * @return The type of grip this weapon uses
     */
    public Set<FireMode> getFireModes() {
//            if(fireMode == null || fireMode.isEmpty())
//                fireMode = new ArrayList<>(List.of(FireMode.SEMI_AUTO));
        return this.fireMode;
    }

    /**
     * @return If this gun need a full charge to fire
     */
    public boolean isFullCharge() {
        return this.fullCharge;
    }

    public boolean isEnchantable() {
        return this.enchantable;
    }

    public boolean isSilenced() {
        return this.silenced;
    }

    /**
     * @return The fire rate of this weapon in ticks
     */
    public int getRate() {
        return this.rate;
    }

    /**
     * @return The delay before firing
     */
    public int getFireDelay() {
        return this.fireTimer;
    }

    /**
     * @return The type of grip this weapon uses
     */
    public GripType getGripType() {
        return this.gripType;
    }

    /**
     * @return The maximum amount of projectile this weapon can hold
     */
    public int getMaxAmmo() {
        return this.maxAmmo;
    }

    /**
     * @return The amount of projectile to add to the weapon each reload cycle
     */
    public int getReloadAmount() {
        return this.reloadAmount;
    }

    public int getReloadStart() {
        return this.reloadStart;
    }

    /**
     * @return Time to reload the gun
     */
    public int getReloadTime() {
        return this.reloadTime;
    }

    public int getReloadEnd() {
        return this.reloadEnd;
    }

    public int getEquipTime() {
        return this.equipTime;
    }

    public int getAmmoPerShot() {
        return this.ammoPerShot;
    }

    public WeaponMode getWeaponMode() {
        return weaponMode;
    }

    /**
     * @return Type of loading
     */
    public LoadingType getLoadingType() {
        return this.loadingType;
    }

    /**
     * @return If weapon should automatically reload if it's empty
     */
    public boolean isAutoReloading() {
        return this.autoReload;
    }

    /**
     * @return If weapon HUD should be rendered
     */
    public boolean shouldRenderHud() {
        return this.renderHud;
    }

    /**
     * @return Weapon category
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * @return The amount of recoil this gun produces upon firing in degrees
     */
    public float getRecoilAngle() {
        return this.recoilAngle;
    }

    /**
     * @return The damage caused by this gun
     */
    public float getDamage() {
        return this.damage;
    }

    /**
     * @return The amount of kick this gun produces upon firing
     */
    public float getRecoilKick() {
        return this.recoilKick;
    }

    /**
     * @return The duration offset for recoil. This reduces the duration of recoil animation
     */
    public float getRecoilDurationOffset() {
        return this.recoilDurationOffset;
    }

    /**
     * @return The amount of reduction applied when aiming down this weapon's sight
     */
    public float getRecoilAdsReduction() {
        return this.recoilAdsReduction;
    }

    /**
     * @return The amount of ammoData this weapon fires
     */
    public int getProjectileAmount() {
        return this.projectileAmount;
    }

    public int getMultishotAmount() {
        return multishotAmount;
    }

    /**
     * @return If this weapon should always spread its ammoData according to {@link #getSpread()}
     */
    public boolean isAlwaysSpread() {
        return this.alwaysSpread;
    }

    public boolean isOneTimeCharge() {
        return this.oneTimeCharge;
    }

    public boolean canMelee() {
        return melee;
    }

    /**
     * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
     * the fired projectile.
     */
    public float getSpread() {
        return this.spread;
    }

    public float getMovementSpeed() {
        return this.movementSpeed;
    }
}

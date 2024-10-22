package com.nukateam.ntgl.common.base.config.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.holders.FireMode;
import com.nukateam.ntgl.common.base.holders.GripType;
import com.nukateam.ntgl.common.base.holders.LoadingType;
import com.nukateam.ntgl.common.base.utils.NbtUtils;
import com.nukateam.ntgl.common.data.annotation.Ignored;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class General implements INBTSerializable<CompoundTag> {
    public static final String LOADING_TYPE = "LoadingType";
    public static final String RATE = "Rate";
    public static final String GRIP_TYPE = "GripType";
    public static final String RELOAD_TYPE = "ReloadType";
    public static final String MAX_AMMO = "MaxAmmo";
    public static final String RELOAD_SPEED = "ReloadSpeed";
    public static final String RELOAD_TIME = "ReloadTime";
    public static final String RECOIL_ANGLE = "RecoilAngle";
    public static final String DAMAGE = "Damage";
    public static final String RECOIL_KICK = "RecoilKick";
    public static final String RECOIL_DURATION_OFFSET = "RecoilDurationOffset";
    public static final String RECOIL_ADS_REDUCTION = "RecoilAdsReduction";
    public static final String PROJECTILE_AMOUNT = "ProjectileAmount";
    public static final String ALWAYS_SPREAD = "AlwaysSpread";
    public static final String SPREAD = "Spread";
    public static final String CATEGORY = "category";

    @Optional
    Set<FireMode> fireMode = new HashSet<>(List.of(FireMode.SEMI_AUTO));
    @Optional
    boolean fullCharge = false;
    int rate;
    @Optional
    float damage;
    @Ignored
    GripType gripType = GripType.ONE_HANDED;
    @Ignored
    ResourceLocation reloadType = new ResourceLocation(Ntgl.MOD_ID, "gun_reload");
    int maxAmmo;
    @Optional
    int reloadTime = 1;
    @Optional
    LoadingType loadingType = LoadingType.MAGAZINE;
    @Optional
    String category = "pistol";
    @Optional
    int reloadAmount = 1;
    @Optional
    float recoilAngle;
    @Optional
    float recoilKick;
    @Optional
    float recoilDurationOffset;
    @Optional
    float recoilAdsReduction = 0.2F;
    @Optional
    int projectileAmount = 1;
    @Optional
    boolean alwaysSpread;
    @Optional
    float spread;
    @Optional
    int fireTimer;
    @Optional
    protected Set<ResourceLocation> ammo = new HashSet<>(List.of(new ResourceLocation("ntgl:round10mm")));

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(RATE, this.rate);
        tag.putBoolean("FullCharge", this.fullCharge);
        tag.putInt("FireTimer", this.fireTimer);
        tag.put("FireMode", NbtUtils.serializeSet(this.fireMode));
        tag.putString(GRIP_TYPE, this.gripType.getId().toString());
        tag.putString(RELOAD_TYPE, this.reloadType.toString());
        tag.putInt(MAX_AMMO, this.maxAmmo);
        tag.putInt(RELOAD_SPEED, this.reloadAmount);
        tag.putInt(RELOAD_TIME, this.reloadTime);
        tag.putString(LOADING_TYPE, this.loadingType.toString());
        tag.putString(CATEGORY, this.category);
        tag.putFloat(RECOIL_ANGLE, this.recoilAngle);
        tag.putFloat(DAMAGE, this.damage);
        tag.putFloat(RECOIL_KICK, this.recoilKick);
        tag.putFloat(RECOIL_DURATION_OFFSET, this.recoilDurationOffset);
        tag.putFloat(RECOIL_ADS_REDUCTION, this.recoilAdsReduction);
        tag.putInt(PROJECTILE_AMOUNT, this.projectileAmount);
        tag.putFloat(SPREAD, this.spread);
        tag.putBoolean(ALWAYS_SPREAD, this.alwaysSpread);
        tag.put("Ammo", NbtUtils.serializeSet(this.ammo));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("FireMode", Tag.TAG_COMPOUND)) {
            this.fireMode = NbtUtils.deserializeFireMode(tag.getCompound("FireMode"));
        }
        if (tag.contains("FullCharge", Tag.TAG_ANY_NUMERIC)) {
            this.fullCharge = tag.getBoolean("FullCharge");
        }
        if (tag.contains(RATE, Tag.TAG_ANY_NUMERIC)) {
            this.rate = tag.getInt(RATE);
        }
        if (tag.contains("FireTimer", Tag.TAG_ANY_NUMERIC)) {
            this.fireTimer = tag.getInt("FireTimer");
        }
        if (tag.contains(GRIP_TYPE, Tag.TAG_STRING)) {
            this.gripType = GripType.getType(ResourceLocation.tryParse(tag.getString(GRIP_TYPE)));
        }
        if (tag.contains(RELOAD_TYPE, Tag.TAG_STRING)) {
            this.reloadType = ResourceLocation.tryParse(tag.getString(RELOAD_TYPE));
        }
        if (tag.contains(MAX_AMMO, Tag.TAG_ANY_NUMERIC)) {
            this.maxAmmo = tag.getInt(MAX_AMMO);
        }
        if (tag.contains(RELOAD_SPEED, Tag.TAG_ANY_NUMERIC)) {
            this.reloadAmount = tag.getInt(RELOAD_SPEED);
        }
        if (tag.contains(RELOAD_TIME, Tag.TAG_ANY_NUMERIC)) {
            this.reloadTime = tag.getInt(RELOAD_TIME);
        }
        if (tag.contains(LOADING_TYPE, Tag.TAG_STRING)) {
            this.loadingType = LoadingType.getType(tag.getString(LOADING_TYPE));
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
        if (tag.contains(ALWAYS_SPREAD, Tag.TAG_ANY_NUMERIC)) {
            this.alwaysSpread = tag.getBoolean(ALWAYS_SPREAD);
        }
        if (tag.contains(SPREAD, Tag.TAG_ANY_NUMERIC)) {
            this.spread = tag.getFloat(SPREAD);
        }
        if (tag.contains("Ammo", Tag.TAG_COMPOUND)) {
            this.ammo = NbtUtils.deserializeAmmoSet(tag.getCompound("Ammo"));
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.rate > 0, "Rate must be more than zero");
        Preconditions.checkArgument(this.maxAmmo > 0, "Max ammo must be more than zero");
        Preconditions.checkArgument(this.reloadAmount >= 1, "Reload amount must be more than or equal to zero");
        Preconditions.checkArgument(this.reloadTime >= 1, "Reload time must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilAngle >= 0.0F, "Recoil angle must be more than or equal to zero");
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage angle must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilKick >= 0.0F, "Recoil kick must be more than or equal to zero");
        Preconditions.checkArgument(this.recoilDurationOffset >= 0.0F && this.recoilDurationOffset <= 1.0F, "Recoil duration offset must be between 0.0 and 1.0");
        Preconditions.checkArgument(this.recoilAdsReduction >= 0.0F && this.recoilAdsReduction <= 1.0F, "Recoil ads reduction must be between 0.0 and 1.0");
        Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
        Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");
        JsonObject object = new JsonObject();
        if (this.fullCharge) object.addProperty("fullCharge", true);
        object.addProperty("rate", this.rate);
        if (this.fireTimer != 0) object.addProperty("fireTimer", this.fireTimer);
//            object.addProperty("fireMode", this.fireMode.getId().toString());
        object.addProperty("gripType", this.gripType.toString());
        object.addProperty("loadingType", this.loadingType.toString());
        object.addProperty("reloadType", this.reloadType.toString());
        object.addProperty("maxAmmo", this.maxAmmo);
        if (this.reloadAmount != 1) object.addProperty("reloadAmount", this.reloadAmount);
        if (this.reloadTime != 1) object.addProperty("reloadTime", this.reloadTime);
        if (this.recoilAngle != 0.0F) object.addProperty("recoilAngle", this.recoilAngle);
        if (this.damage != 0.0F) object.addProperty("damage", this.damage);
        if (this.recoilKick != 0.0F) object.addProperty("recoilKick", this.recoilKick);
        if (this.recoilDurationOffset != 0.0F)
            object.addProperty("recoilDurationOffset", this.recoilDurationOffset);
        if (this.recoilAdsReduction != 0.2F) object.addProperty("recoilAdsReduction", this.recoilAdsReduction);
        if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
        if (this.alwaysSpread) object.addProperty("alwaysSpread", true);
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
        general.rate = this.rate;
        general.fireTimer = this.fireTimer;
        general.gripType = this.gripType;
        general.reloadType = this.reloadType;
        general.maxAmmo = this.maxAmmo;
        general.reloadAmount = this.reloadAmount;
        general.reloadTime = this.reloadTime;
        general.loadingType = this.loadingType;
        general.category = this.category;
        general.recoilAngle = this.recoilAngle;
        general.damage = this.damage;
        general.recoilKick = this.recoilKick;
        general.recoilDurationOffset = this.recoilDurationOffset;
        general.recoilAdsReduction = this.recoilAdsReduction;
        general.projectileAmount = this.projectileAmount;
        general.alwaysSpread = this.alwaysSpread;
        general.spread = this.spread;
        general.ammo = new HashSet<>(this.ammo);
        return general;
    }

    public Set<ResourceLocation> getAmmo() {
        return this.ammo;
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

    /**
     * @return The fire rate of this weapon in ticks
     */
    public int getRate() {
        return this.rate;
    }

    /**
     * @return The timer before firing
     */
    public int getFireTimer() {
        return this.fireTimer;
    }

    /**
     * @return The type of grip this weapon uses
     */
    public GripType getGripType() {
        return this.gripType;
    }

    public ResourceLocation getReloadType() {
        return this.reloadType;
    }

    /**
     * @return The maximum amount of ammo this weapon can hold
     */
    public int getMaxAmmo(@Nullable ItemStack gunStack) {
        if (gunStack != null && gunStack.getItem() instanceof GunItem gunItem) {
            var gun = gunItem.getModifiedGun(gunStack);

            if (GunModifierHelper.getCurrentProjectile(gunStack).isMagazineMode()) {
                var id = GunModifierHelper.getCurrentAmmo(gunStack);
                var item = ForgeRegistries.ITEMS.getValue(id);

                return item.getMaxDamage(new ItemStack(item));
            }

        }
        return this.maxAmmo;
    }

    /**
     * @return The amount of ammo to add to the weapon each reload cycle
     */
    public int getReloadAmount() {
        return this.reloadAmount;
    }

    /**
     * @return Time to reload the gun
     */
    public int getReloadTime() {
        return this.reloadTime;
    }

    /**
     * @return Type of loading
     */
    public LoadingType getLoadingType() {
        return this.loadingType;
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
     * @return The damage caused by this ammo
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
     * @return The amount of projectiles this weapon fires
     */
    public int getProjectileAmount() {
        return this.projectileAmount;
    }

    /**
     * @return If this weapon should always spread it's projectiles according to {@link #getSpread()}
     */
    public boolean isAlwaysSpread() {
        return this.alwaysSpread;
    }

    /**
     * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
     * the fired ammo.
     */
    public float getSpread() {
        return this.spread;
    }
}

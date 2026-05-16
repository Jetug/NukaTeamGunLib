package com.nukateam.ntgl.common.data.config.weapon;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.ProjectileType;
import com.nukateam.ntgl.common.data.holders.ProjectileVariant;
import com.nukateam.ntgl.common.foundation.init.NtglDamageTypes;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.GunJsonUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;

import static com.nukateam.ntgl.common.data.json.JsonDeserializers.getDamageTypeResourceKey;
import static com.nukateam.ntgl.common.data.config.weapon.General.PROJECTILE_AMOUNT;
import static com.nukateam.ntgl.common.data.config.weapon.General.SPREAD;

public class ProjectileConfig implements INBTSerializable<CompoundTag> {
    private float damage = 1;
    private float size;
    @Optional private float speed = 20;
    private int life = 20;
    @Optional private ProjectileType projectile = ProjectileType.BULLET;
    @Optional private ResourceKey<DamageType> damageType = NtglDamageTypes.BULLET;
    @Optional private boolean visible;
    @Optional private boolean gravity;
    @Optional private boolean affectedByFluid = true;
    @Optional private boolean damageReduceOverLife;
    @Optional private boolean magazineMode;
    @Optional private int trailColor = 0xFFD289;
    @Optional private int color = 0xFFFFFF;
    @Optional private ProjectileVariant variant = ProjectileVariant.STANDARD;
    @Optional private double trailLengthMultiplier = 1.0;
    @Optional int projectileAmount = 1;
    @Optional float spread;
    @Optional int pierceLevel = 0;
    @Optional int burnSeconds = 0;
    @Optional ExplosionConfig explosion = new ExplosionConfig();
    @Optional @Nullable
    ResourceLocation hitSound;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("Projectile", this.projectile.toString());
        tag.putString("Variant", this.variant.toString());
        tag.putString("DamageType", this.damageType.location().toString());
        tag.putFloat("Damage", this.damage);
        tag.putBoolean("Visible", this.visible);
        tag.putFloat("Size", this.size);
        tag.putFloat("Speed", this.speed);
        tag.putInt("Life", this.life);
        tag.putBoolean("Gravity", this.gravity);
        tag.putBoolean("affectedByFluid", this.affectedByFluid);
        tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
        tag.putBoolean("MagazineMode", this.magazineMode);
        tag.putInt("TrailColor", this.trailColor);
        tag.putInt("Color", this.color);
        tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
        tag.putInt(PROJECTILE_AMOUNT, this.projectileAmount);
        tag.putFloat(SPREAD, this.spread);
        tag.putInt("pierceLevel", this.pierceLevel);
        tag.putInt("burnSeconds", this.burnSeconds);
        tag.put("explosion", this.explosion.serializeNBT(provider));
        if(hitSound != null)
            tag.putString("hitSound", this.hitSound.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("Visible", Tag.TAG_ANY_NUMERIC)) {
            this.visible = tag.getBoolean("Visible");
        }
        if (tag.contains("DamageType", Tag.TAG_STRING)) {
            this.damageType = getDamageTypeResourceKey(tag.getString("DamageType"));
        }
        if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat("Damage");
        }
        if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
            this.size = tag.getFloat("Size");
        }
        if (tag.contains("Speed", Tag.TAG_ANY_NUMERIC)) {
            this.speed = tag.getFloat("Speed");
        }
        if (tag.contains("Life", Tag.TAG_ANY_NUMERIC)) {
            this.life = tag.getInt("Life");
        }
        if (tag.contains("pierceLevel", Tag.TAG_ANY_NUMERIC)) {
            this.pierceLevel = tag.getInt("pierceLevel");
        }
        if (tag.contains("burnSeconds", Tag.TAG_ANY_NUMERIC)) {
            this.burnSeconds = tag.getInt("burnSeconds");
        }
        if (tag.contains("Gravity", Tag.TAG_ANY_NUMERIC)) {
            this.gravity = tag.getBoolean("Gravity");
        }
        if (tag.contains("affectedByFluid", Tag.TAG_ANY_NUMERIC)) {
            this.affectedByFluid = tag.getBoolean("affectedByFluid");
        }
        if (tag.contains("DamageReduceOverLife", Tag.TAG_ANY_NUMERIC)) {
            this.damageReduceOverLife = tag.getBoolean("DamageReduceOverLife");
        }
        if (tag.contains("MagazineMode", Tag.TAG_ANY_NUMERIC)) {
            this.magazineMode = tag.getBoolean("MagazineMode");
        }
        if (tag.contains("TrailColor", Tag.TAG_ANY_NUMERIC)) {
            this.trailColor = tag.getInt("TrailColor");
        }
        if (tag.contains("Color", Tag.TAG_ANY_NUMERIC)) {
            this.color = tag.getInt("Color");
        }
        if (tag.contains("TrailLengthMultiplier", Tag.TAG_ANY_NUMERIC)) {
            this.trailLengthMultiplier = tag.getDouble("TrailLengthMultiplier");
        }
        if (tag.contains("Projectile", Tag.TAG_STRING)) {
            this.projectile = ProjectileType.getType(tag.getString("Projectile"));
        }
        if (tag.contains("Variant", Tag.TAG_STRING)) {
            this.variant = ProjectileVariant.getType(tag.getString("Variant"));
        }
        if (tag.contains(PROJECTILE_AMOUNT, Tag.TAG_ANY_NUMERIC)) {
            this.projectileAmount = tag.getInt(PROJECTILE_AMOUNT);
        }
        if (tag.contains(SPREAD, Tag.TAG_ANY_NUMERIC)) {
            this.spread = tag.getFloat(SPREAD);
        }
        if (tag.contains("explosion", Tag.TAG_COMPOUND)) {
            this.explosion = ExplosionConfig.create(tag.getCompound("explosion"));
        }
        if (tag.contains("hitSound", Tag.TAG_STRING)) {
            this.hitSound = ResourceLocation.tryParse(tag.getString("hitSound"));
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        Preconditions.checkArgument(this.size >= 0.0F, "Projectile size must be more than or equal to zero");
        Preconditions.checkArgument(this.speed >= 0.0, "Projectile speed must be more than or equal to zero");
        Preconditions.checkArgument(this.life > 0, "Projectile life must be more than zero");
        Preconditions.checkArgument(this.trailLengthMultiplier >= 0.0, "Projectile trail length multiplier must be more than or equal to zero");
        Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
        Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");

        var object = new JsonObject();

        if (this.visible) object.addProperty("visible", true);
        object.addProperty("damage", this.damage);
        object.addProperty("size", this.size);
        object.addProperty("speed", this.speed);
        object.addProperty("life", this.life);
        object.addProperty("pierceLevel", this.pierceLevel);
        object.addProperty("burnSeconds", this.burnSeconds);
        object.addProperty("projectile", this.projectile.toString());
        object.addProperty("variant", this.variant.toString());
        object.addProperty("damageType", this.damageType.location().toString());
        object.addProperty("hitSound", this.hitSound.toString());
        GunJsonUtil.addObjectIfNotEmpty(object,"explosion", this.explosion.toJsonObject());

        if (this.gravity) object.addProperty("gravity", true);
        object.addProperty("affectedByFluid", true);
        object.addProperty("damageReduceOverLife", this.damageReduceOverLife);
        object.addProperty("magazineMode", this.magazineMode);
        if (this.trailColor != 0xFFD289) object.addProperty("trailColor", this.trailColor);
        if (this.color != 0xFFFFFF) object.addProperty("color", this.color);
        if (this.trailLengthMultiplier != 1.0) object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
        if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
        if (this.spread != 0.0F) object.addProperty("spread", this.spread);
        return object;
    }

    public ProjectileConfig copy() {
        var projectile = new ProjectileConfig();
        projectile.visible = this.visible;
        projectile.damage = this.damage;
        projectile.size = this.size;
        projectile.speed = this.speed;
        projectile.life = this.life;
        projectile.pierceLevel = this.pierceLevel;
        projectile.burnSeconds = this.burnSeconds;
        projectile.gravity = this.gravity;
        projectile.affectedByFluid = this.affectedByFluid;
        projectile.damageReduceOverLife = this.damageReduceOverLife;
        projectile.magazineMode = this.magazineMode;
        projectile.trailColor = this.trailColor;
        projectile.color = this.color;
        projectile.trailLengthMultiplier = this.trailLengthMultiplier;
        projectile.projectile = this.projectile;
        projectile.variant = this.variant;
        projectile.damageType = this.damageType;
        projectile.projectileAmount = this.projectileAmount;
        projectile.spread = this.spread;
        projectile.explosion = this.explosion;
        projectile.hitSound = this.hitSound;
        return projectile;
    }

    public ExplosionConfig getExplosion() {
        return explosion;
    }

    public @Nullable ResourceLocation getHitSound() {
        return hitSound;
    }

    /**
     * @return If this projectile should be visible when rendering
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * @return The damage caused by this projectile
     */
    public float getDamage() {
        return this.damage;
    }

    /**
     * @return The size of the projectile entity bounding box
     */
    public float getSize() {
        return this.size;
    }

    /**
     * @return The speed the projectile moves every tick
     */
    public float getSpeed() {
        return this.speed;
    }

    /**
     * @return The amount of ticks before this projectile is removed
     */
    public int getLife() {
        return this.life;
    }

    public int getPierceLevel() {
        return pierceLevel;
    }

    public int getBurnSeconds() {
        return burnSeconds;
    }

    /**
     * @return If gravity should be applied to the projectile
     */
    public boolean isGravity() {
        return this.gravity;
    }

    public boolean affectedByFluid() {
        return this.affectedByFluid;
    }

    /**
     * @return If the damage should reduce the further the projectiletravels
     */
    public boolean isDamageReduceOverLife() {
        return this.damageReduceOverLife;
    }


    public boolean isMagazineMode() {
        return this.magazineMode;
    }

    /**
     * @return The color of the projectile trail in rgba integer format
     */
    public int getTrailColor() {
        return this.trailColor;
    }

    public int getColor() {
        return this.color;
    }

    /**
     * @return The multiplier to change the length of the projectile trail
     */
    public double getTrailLengthMultiplier() {
        return this.trailLengthMultiplier;
    }

    /**
     * @return The amount of ammoData this weapon fires
     */
    public int getProjectileAmount() {
        return this.projectileAmount;
    }

    /**
     * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
     * the fired projectile
     */
    public float getSpread() {
        return this.spread;
    }

    public ProjectileType getProjectileType() {
        return this.projectile;
    }

    public ProjectileVariant getProjectileVariant() {
        return this.variant;
    }

    public ResourceKey<DamageType> getDamageType() {
        return this.damageType;
    }

    public static ProjectileConfig create(CompoundTag tag) {
        var ammo = new ProjectileConfig();
        ammo.deserializeNBT(null, tag);
        return ammo;
    }

    public static class Builder {
        private final ProjectileConfig projectile;

        private Builder() {
            this.projectile = new ProjectileConfig();
        }

        private Builder(ProjectileConfig projectile) {
            this.projectile = projectile.copy();
        }

        public static ProjectileConfig.Builder create() {
            return new ProjectileConfig.Builder();
        }

        public static ProjectileConfig.Builder create(ProjectileConfig projectile) {
            return new ProjectileConfig.Builder(projectile);
        }

        public ProjectileConfig build() {
            return this.projectile.copy(); //Copy since the builder could be used again
        }

        public ProjectileConfig.Builder setProjectileVisible(boolean visible) {
            this.projectile.visible = visible;
            return this;
        }

        public ProjectileConfig.Builder setProjectileAmount(int projectileAmount) {
            this.projectile.projectileAmount = projectileAmount;
            return this;
        }

        public ProjectileConfig.Builder setProjectileSize(float size) {
            this.projectile.size = size;
            return this;
        }

        public ProjectileConfig.Builder setProjectileSpeed(float speed) {
            this.projectile.speed = speed;
            return this;
        }

        public ProjectileConfig.Builder setProjectileLife(int life) {
            this.projectile.life = life;
            return this;
        }

        public ProjectileConfig.Builder setPierceLevel(int pierceLevel) {
            this.projectile.pierceLevel = pierceLevel;
            return this;
        }

        public ProjectileConfig.Builder setProjectileAffectedByGravity(boolean gravity) {
            this.projectile.gravity = gravity;
            return this;
        }

        public ProjectileConfig.Builder setProjectileTrailColor(int trailColor) {
            this.projectile.trailColor = trailColor;
            return this;
        }

        public ProjectileConfig.Builder setProjectileTrailLengthMultiplier(int trailLengthMultiplier) {
            this.projectile.trailLengthMultiplier = trailLengthMultiplier;
            return this;
        }

        public ProjectileConfig.Builder setDamage(float damage) {
            this.projectile.damage = damage;
            return this;
        }

        public ProjectileConfig.Builder setSpread(float spread) {
            this.projectile.spread = spread;
            return this;
        }

        public ProjectileConfig.Builder setReduceDamageOverLife(boolean damageReduceOverLife) {
            this.projectile.damageReduceOverLife = damageReduceOverLife;
            return this;
        }

        public ProjectileConfig.Builder setMagazineMode(boolean magazineMode) {
            this.projectile.magazineMode = magazineMode;
            return this;
        }

        public ProjectileConfig.Builder setExplosionConfig(ExplosionConfig explosion) {
            this.projectile.explosion = explosion;
            return this;
        }

        public ProjectileConfig.Builder setProjectileType(ProjectileType projectile) {
            this.projectile.projectile = projectile;
            return this;
        }

        public ProjectileConfig.Builder setDamageType(ResourceKey<DamageType> damageType) {
            this.projectile.damageType = damageType;
            return this;
        }

        public ProjectileConfig.Builder setAffectedByFluid(boolean affectedByFluid) {
            this.projectile.affectedByFluid = affectedByFluid;
            return this;
        }

        public ProjectileConfig.Builder setColor(int color) {
            this.projectile.color = color;
            return this;
        }

        public ProjectileConfig.Builder setBurnSeconds(int burnSeconds) {
            this.projectile.burnSeconds = burnSeconds;
            return this;
        }

        public ProjectileConfig.Builder setHitSound(@Nullable ResourceLocation hitSound) {
            this.projectile.hitSound = hitSound;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectileConfig that)) return false;

        return Float.compare(that.damage, damage) == 0
                && Float.compare(that.size, size) == 0
                && Float.compare(that.speed, speed) == 0
                && life == that.life
                && visible == that.visible
                && gravity == that.gravity
                && affectedByFluid == that.affectedByFluid
                && damageReduceOverLife == that.damageReduceOverLife
                && magazineMode == that.magazineMode
                && trailColor == that.trailColor
                && Double.compare(that.trailLengthMultiplier, trailLengthMultiplier) == 0
                && projectileAmount == that.projectileAmount
                && Float.compare(that.spread, spread) == 0
                && pierceLevel == that.pierceLevel
                && burnSeconds == that.burnSeconds
                && java.util.Objects.equals(projectile, that.projectile)
                && java.util.Objects.equals(damageType, that.damageType)
                && java.util.Objects.equals(explosion, that.explosion)
                && java.util.Objects.equals(hitSound, that.hitSound);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                damage,
                size,
                speed,
                life,
                projectile,
                damageType,
                visible,
                gravity,
                affectedByFluid,
                damageReduceOverLife,
                magazineMode,
                trailColor,
                trailLengthMultiplier,
                projectileAmount,
                spread,
                pierceLevel,
                burnSeconds,
                explosion,
                hitSound
        );
    }
}

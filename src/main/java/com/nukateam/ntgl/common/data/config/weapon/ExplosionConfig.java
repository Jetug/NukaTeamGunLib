package com.nukateam.ntgl.common.data.config.weapon;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class ExplosionConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String DAMAGE = "Damage";
    public static final String DISTANCE = "DamageReduceOverDistance";
    public static final String CAUSE_FIRE = "CauseFire";
    public static final String EXPLOSION_RADIUS = "ExplosionRadius";
    public static final String DESTROY_BLOCKS = "DestroyBlocks";
    public static final String EXPLODE_ON_CONTACT = "explodeOnContact";
    public static final String KNOCKBACK = "knockback";

    @Optional private float damage = 0;
    @Optional private boolean damageReduceOverDistance = true;
    @Optional private boolean causeFire = false;
    @Optional private boolean destroyBlocks = false;
    @Optional private boolean explodeOnContact = true;
    @Optional private float radius;
    @Optional private float knockback;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putFloat(DAMAGE, this.damage);
        tag.putFloat(EXPLOSION_RADIUS, this.radius);
        tag.putFloat(KNOCKBACK, this.knockback);
        tag.putBoolean(DISTANCE, this.damageReduceOverDistance);
        tag.putBoolean(CAUSE_FIRE, this.causeFire);
        tag.putBoolean(DESTROY_BLOCKS, this.destroyBlocks);
        tag.putBoolean(EXPLODE_ON_CONTACT, this.explodeOnContact);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat(DAMAGE);
        }
        if (tag.contains(EXPLOSION_RADIUS, Tag.TAG_ANY_NUMERIC)) {
            this.radius = tag.getFloat(EXPLOSION_RADIUS);
        }
        if (tag.contains(KNOCKBACK, Tag.TAG_ANY_NUMERIC)) {
            this.knockback = tag.getFloat(KNOCKBACK);
        }
        if (tag.contains(DISTANCE, Tag.TAG_ANY_NUMERIC)) {
            this.damageReduceOverDistance = tag.getBoolean(DISTANCE);
        }
        if (tag.contains(CAUSE_FIRE, Tag.TAG_ANY_NUMERIC)) {
            this.causeFire = tag.getBoolean(CAUSE_FIRE);
        }
        if (tag.contains(DESTROY_BLOCKS, Tag.TAG_ANY_NUMERIC)) {
            this.destroyBlocks = tag.getBoolean(DESTROY_BLOCKS);
        }
        if (tag.contains(EXPLODE_ON_CONTACT, Tag.TAG_ANY_NUMERIC)) {
            this.explodeOnContact = tag.getBoolean(EXPLODE_ON_CONTACT);
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        var object = new JsonObject();
        object.addProperty("damage", this.damage);
        object.addProperty("radius", this.radius);
        object.addProperty("knockback", this.knockback);
        object.addProperty("damageReduceOverDistance", this.damageReduceOverDistance);
        object.addProperty("causeFire", causeFire);
        object.addProperty("destroyBlocks", this.destroyBlocks);
        object.addProperty("explodeOnContact", this.explodeOnContact);
        return object;
    }

    public ExplosionConfig copy() {
        var projectile = new ExplosionConfig();
        projectile.damage = this.damage;
        projectile.knockback = this.knockback;
        projectile.damageReduceOverDistance = this.damageReduceOverDistance;
        projectile.causeFire = this.causeFire;
        projectile.radius = this.radius;
        projectile.destroyBlocks = this.destroyBlocks;
        projectile.explodeOnContact = this.explodeOnContact;
        return projectile;
    }

    /**
     * @return The damage caused by the projectile explosion
     */
    public float getDamage() {
        return this.damage;
    }

    public boolean isDamageReduceOverDistance() {
        return this.damageReduceOverDistance;
    }

    public boolean isCauseFire() {
        return causeFire;
    }

    public boolean isExplodeOnContact() {
        return explodeOnContact;
    }

    public float getKnockback() {
        return knockback;
    }

    /**
     * @return The radius of explosion caused by this projectile (0 if no explosion)
     */
    public float getRadius() {
        return this.radius;
    }

    public boolean isDestroyBlocks() {
        return destroyBlocks;
    }

    public static ExplosionConfig create(CompoundTag tag) {
        var ammo = new ExplosionConfig();
        ammo.deserializeNBT(tag);
        return ammo;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Projectile");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {});
    }

    public static class Builder {
        private final ExplosionConfig config;

        private Builder() {
            this.config = new ExplosionConfig();
        }

        private Builder(ExplosionConfig projectile) {
            this.config = projectile.copy();
        }

        public static ExplosionConfig.Builder create() {
            return new ExplosionConfig.Builder();
        }

        public static ExplosionConfig.Builder create(ExplosionConfig projectile) {
            return new ExplosionConfig.Builder(projectile);
        }

        public ExplosionConfig build() {
            return this.config.copy(); //Copy since the builder could be used again
        }

        public ExplosionConfig.Builder setDamage(float damage) {
            this.config.damage = damage;
            return this;
        }
        
        public ExplosionConfig.Builder setRadius(float value) {
            this.config.radius = value;
            return this;
        }

        public void setDamageReduceOverDistance(boolean damageReduceOverDistance) {
            this.config.damageReduceOverDistance = damageReduceOverDistance;
        }

        public void setCauseFire(boolean causeFire) {
            this.config.causeFire = causeFire;
        }

        public void setDestroyBlocks(boolean destroyBlocks) {
            this.config.destroyBlocks = destroyBlocks;
        }

        public void setExplodeOnContact(boolean explodeOnContact) {
            this.config.explodeOnContact = explodeOnContact;
        }

        public void setKnockback(float knockback) {
            this.config.knockback = knockback;
        }
    }
}

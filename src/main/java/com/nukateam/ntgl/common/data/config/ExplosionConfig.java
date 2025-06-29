package com.nukateam.ntgl.common.data.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class ExplosionConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String DAMAGE = "Damage";
    public static final String DISTANCE = "DamageReduceOverDistance";
    public static final String CAUSE_FIRE = "CauseFire";
    public static final String EXPLOSION_RADIUS = "ExplosionRadius";
    public static final String DESTROY_BLOCKS = "DestroyBlocks";

    @Optional private float damage = 0;
    @Optional private boolean damageReduceOverDistance = true;
    @Optional private boolean causeFire = false;
    @Optional private boolean destroyBlocks = false;
    @Optional private float radius;

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat(DAMAGE, this.damage);
        tag.putBoolean(DISTANCE, this.damageReduceOverDistance);
        tag.putBoolean(CAUSE_FIRE, this.causeFire);
        tag.putBoolean(DESTROY_BLOCKS, this.destroyBlocks);
        tag.putFloat(EXPLOSION_RADIUS, this.radius);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat(DAMAGE);
        }
        if (tag.contains(DISTANCE, Tag.TAG_ANY_NUMERIC)) {
            this.damageReduceOverDistance = tag.getBoolean(DISTANCE);
        }
        if (tag.contains(DESTROY_BLOCKS, Tag.TAG_ANY_NUMERIC)) {
            this.causeFire = tag.getBoolean(CAUSE_FIRE);
        }
        if (tag.contains(CAUSE_FIRE, Tag.TAG_ANY_NUMERIC)) {
            this.destroyBlocks = tag.getBoolean(DESTROY_BLOCKS);
        }
        if (tag.contains(EXPLOSION_RADIUS, Tag.TAG_ANY_NUMERIC)) {
            this.radius = tag.getFloat(EXPLOSION_RADIUS);
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        var object = new JsonObject();
        object.addProperty("damage", this.damage);
        object.addProperty("damageReduceOverDistance", damageReduceOverDistance);
        object.addProperty("causeFire", causeFire);
        object.addProperty("radius", this.radius);
        object.addProperty("destroyBlocks", this.destroyBlocks);
        return object;
    }

    public ExplosionConfig copy() {
        var projectile = new ExplosionConfig();
        projectile.damage = this.damage;
        projectile.damageReduceOverDistance = this.damageReduceOverDistance;
        projectile.causeFire = this.causeFire;
        projectile.radius = this.radius;
        projectile.destroyBlocks = this.destroyBlocks;
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
        private final ExplosionConfig projectile;

        private Builder() {
            this.projectile = new ExplosionConfig();
        }

        private Builder(ExplosionConfig projectile) {
            this.projectile = projectile.copy();
        }

        public static ExplosionConfig.Builder create() {
            return new ExplosionConfig.Builder();
        }

        public static ExplosionConfig.Builder create(ExplosionConfig projectile) {
            return new ExplosionConfig.Builder(projectile);
        }

        public ExplosionConfig build() {
            return this.projectile.copy(); //Copy since the builder could be used again
        }

        public ExplosionConfig.Builder setDamage(ResourceLocation id, float damage) {
            this.projectile.damage = damage;
            return this;
        }
    }
}

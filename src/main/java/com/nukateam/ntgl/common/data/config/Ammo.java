package com.nukateam.ntgl.common.data.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.base.holders.AmmoType;
import com.nukateam.ntgl.common.base.holders.ProjectileType;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.function.Supplier;

import static com.nukateam.ntgl.common.base.utils.json.JsonDeserializers.getDamageTypeResourceKey;
import static com.nukateam.ntgl.common.data.config.gun.General.PROJECTILE_AMOUNT;
import static com.nukateam.ntgl.common.data.config.gun.General.SPREAD;

public class Ammo implements INBTSerializable<CompoundTag>, IEditorMenu {
    private float damage = 1;
    private float size;
    private double speed = 20;
    private int life = 20;
    @Optional private AmmoType type = AmmoType.STANDARD;
    @Optional private ProjectileType projectile = ProjectileType.BULLET;
    @Optional private ResourceKey<DamageType> damageType = ModDamageTypes.BULLET;
    @Optional private boolean visible;
    @Optional private boolean gravity;
    @Optional private boolean damageReduceOverLife;
    @Optional private boolean magazineMode;
    @Optional private int trailColor = 0xFFD289;
    @Optional private double trailLengthMultiplier = 1.0;
    @Optional int projectileAmount = 1;
    @Optional float spread;

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("Type", this.type.toString());
        tag.putString("Projectile", this.projectile.toString());
        tag.putString("DamageType", this.damageType.location().toString());
        tag.putFloat("Damage", this.damage);
        tag.putBoolean("Visible", this.visible);
        tag.putFloat("Size", this.size);
        tag.putDouble("Speed", this.speed);
        tag.putInt("Life", this.life);
        tag.putBoolean("Gravity", this.gravity);
        tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
        tag.putBoolean("MagazineMode", this.magazineMode);
        tag.putInt("TrailColor", this.trailColor);
        tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
        tag.putInt(PROJECTILE_AMOUNT, this.projectileAmount);
        tag.putFloat(SPREAD, this.spread);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
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
            this.speed = tag.getDouble("Speed");
        }
        if (tag.contains("Life", Tag.TAG_ANY_NUMERIC)) {
            this.life = tag.getInt("Life");
        }
        if (tag.contains("Gravity", Tag.TAG_ANY_NUMERIC)) {
            this.gravity = tag.getBoolean("Gravity");
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
        if (tag.contains("TrailLengthMultiplier", Tag.TAG_ANY_NUMERIC)) {
            this.trailLengthMultiplier = tag.getDouble("TrailLengthMultiplier");
        }
        if (tag.contains("Type", Tag.TAG_STRING)) {
            this.type = AmmoType.getType(tag.getString("Type"));
        }
        if (tag.contains("Projectile", Tag.TAG_STRING)) {
            this.projectile = ProjectileType.getType(tag.getString("Projectile"));
        }
        if (tag.contains(PROJECTILE_AMOUNT, Tag.TAG_ANY_NUMERIC)) {
            this.projectileAmount = tag.getInt(PROJECTILE_AMOUNT);
        }
        if (tag.contains(SPREAD, Tag.TAG_ANY_NUMERIC)) {
            this.spread = tag.getFloat(SPREAD);
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

        JsonObject object = new JsonObject();

        if (this.visible) object.addProperty("visible", true);
        object.addProperty("damage", this.damage);
        object.addProperty("size", this.size);
        object.addProperty("speed", this.speed);
        object.addProperty("life", this.life);
        object.addProperty("type", this.type.toString());
        object.addProperty("projectile", this.projectile.toString());
        object.addProperty("damageType", this.damageType.location().toString());

        if (this.gravity) object.addProperty("gravity", true);
        if (this.damageReduceOverLife) object.addProperty("damageReduceOverLife", this.damageReduceOverLife);
        if (this.magazineMode) object.addProperty("magazineMode", this.magazineMode);
        if (this.trailColor != 0xFFD289) object.addProperty("trailColor", this.trailColor);
        if (this.trailLengthMultiplier != 1.0) object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
        if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
        if (this.spread != 0.0F) object.addProperty("spread", this.spread);
        return object;
    }

    public Ammo copy() {
        var projectile = new Ammo();
        projectile.visible = this.visible;
        projectile.damage = this.damage;
        projectile.size = this.size;
        projectile.speed = this.speed;
        projectile.life = this.life;
        projectile.gravity = this.gravity;
        projectile.damageReduceOverLife = this.damageReduceOverLife;
        projectile.magazineMode = this.magazineMode;
        projectile.trailColor = this.trailColor;
        projectile.trailLengthMultiplier = this.trailLengthMultiplier;
        projectile.type = this.type;
        projectile.projectile = this.projectile;
        projectile.damageType = this.damageType;
        projectile.projectileAmount = this.projectileAmount;
        projectile.spread = this.spread;

        return projectile;
    }

    /**
     * @return If this projectile  should be visible when rendering
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
     * @return The size of the projectile  entity bounding box
     */
    public float getSize() {
        return this.size;
    }

    /**
     * @return The speed the projectile  moves every tick
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * @return The amount of ticks before this projectile is removed
     */
    public int getLife() {
        return this.life;
    }

    /**
     * @return If gravity should be applied to the projectile
     */
    public boolean isGravity() {
        return this.gravity;
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

    /**
     * @return The multiplier to change the length of the projectile  trail
     */
    public double getTrailLengthMultiplier() {
        return this.trailLengthMultiplier;
    }

    /**
     * @return The amount of projectiles this weapon fires
     */
    public int getProjectileAmount() {
        return this.projectileAmount;
    }

    /**
     * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
     * the fired projectile.
     */
    public float getSpread() {
        return this.spread;
    }

    public AmmoType getType() {
        return this.type;
    }

    public ProjectileType getProjectile() {
        return this.projectile;
    }

    public ResourceKey<DamageType> getDamageType() {
        return this.damageType;
    }

    public static Ammo create(CompoundTag tag) {
        var ammo = new Ammo();
        ammo.deserializeNBT(tag);
        return ammo;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Projectile");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
//            ItemStack scope = Projectile.getScopeStack(heldItem);
//            if (scope.getItem() instanceof ScopeItem scopeItem) {
//                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.literal("Edit"), btn -> {
//                    Minecraft.getInstance().setScreen(createEditorScreen(Debug.getScope(scopeItem)));
//                })));
//            }

//            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.literal(">"), btn -> {
//                Minecraft.getInstance().setScreen(createEditorScreen(this.modules));
//            })));
        });
    }

    public static class Builder {
        private final Ammo projectile;

        private Builder() {
            this.projectile = new Ammo();
        }

        private Builder(Ammo projectile) {
            this.projectile = projectile.copy();
        }

        public static Ammo.Builder create() {
            return new Ammo.Builder();
        }

        public static Ammo.Builder create(Ammo projectile) {
            return new Ammo.Builder(projectile);
        }

        public Ammo build() {
            return this.projectile.copy(); //Copy since the builder could be used again
        }

        public Ammo.Builder setProjectileVisible(ResourceLocation id, boolean visible) {
            this.projectile.visible = visible;
            return this;
        }

        public Ammo.Builder setProjectileSize(ResourceLocation id, float size) {
            this.projectile.size = size;
            return this;
        }

        public Ammo.Builder setProjectileSpeed(ResourceLocation id, double speed) {
            this.projectile.speed = speed;
            return this;
        }

        public Ammo.Builder setProjectileLife(ResourceLocation id, int life) {
            this.projectile.life = life;
            return this;
        }

        public Ammo.Builder setProjectileAffectedByGravity(ResourceLocation id, boolean gravity) {
            this.projectile.gravity = gravity;
            return this;
        }

        public Ammo.Builder setProjectileTrailColor(ResourceLocation id, int trailColor) {
            this.projectile.trailColor = trailColor;
            return this;
        }

        public Ammo.Builder setProjectileTrailLengthMultiplier(ResourceLocation id, int trailLengthMultiplier) {
            this.projectile.trailLengthMultiplier = trailLengthMultiplier;
            return this;
        }

        public Ammo.Builder setDamage(ResourceLocation id, float damage) {
            this.projectile.damage = damage;
            return this;
        }

        public Ammo.Builder setReduceDamageOverLife(ResourceLocation id, boolean damageReduceOverLife) {
            this.projectile.damageReduceOverLife = damageReduceOverLife;
            return this;
        }

        public Ammo.Builder setMagazineMode(ResourceLocation id, boolean magazineMode) {
            this.projectile.magazineMode = magazineMode;
            return this;
        }
    }
}

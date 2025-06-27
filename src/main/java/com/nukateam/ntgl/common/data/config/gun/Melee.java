package com.nukateam.ntgl.common.data.config.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
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

public class Melee implements INBTSerializable<CompoundTag>, IEditorMenu {
    private float damage = 1;
    private int time = 0;
    private int delay = 0;
    private float distance = 1;
    private float knockback = 0;
    private int maxTargets = 0;
    private float angle = 10;

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat("Damage", this.damage);
        tag.putInt("time", this.time);
        tag.putInt("delay", this.delay);
        tag.putFloat("distance", this.distance);
        tag.putFloat("attackRadius", this.angle);
        tag.putFloat("knockback", this.knockback);
        tag.putInt("maxTargets", this.maxTargets);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat("Damage");
        }
        if (tag.contains("time", Tag.TAG_ANY_NUMERIC)) {
            this.time = tag.getInt("time");
        }
        if (tag.contains("delay", Tag.TAG_ANY_NUMERIC)) {
            this.delay = tag.getInt("delay");
        }
        if (tag.contains("distance", Tag.TAG_ANY_NUMERIC)) {
            this.distance = tag.getFloat("distance");
        }
        if (tag.contains("attackRadius", Tag.TAG_ANY_NUMERIC)) {
            this.angle = tag.getFloat("attackRadius");
        }
        if (tag.contains("knockback", Tag.TAG_ANY_NUMERIC)) {
            this.knockback = tag.getFloat("knockback");
        }
        if (tag.contains("maxTargets", Tag.TAG_ANY_NUMERIC)) {
            this.maxTargets = tag.getInt("maxTargets");
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        var object = new JsonObject();
        object.addProperty("damage", this.damage);
        object.addProperty("time", this.time);
        object.addProperty("delay", this.delay);
        object.addProperty("distance", this.distance);
        object.addProperty("knockback", this.knockback);
        object.addProperty("maxTargets", this.maxTargets);
        object.addProperty("attackRadius", this.angle);
        return object;
    }

    public Melee copy() {
        var projectile = new Melee();
        projectile.damage = this.damage;
        projectile.time = this.time;
        projectile.delay = this.delay;
        projectile.distance = this.distance;
        projectile.knockback = this.knockback;
        projectile.maxTargets = this.maxTargets;
        projectile.angle = this.angle;
        return projectile;
    }


    /**
     * @return The damage caused by melee attack
     */
    public float getDamage() {
        return this.damage;
    }

    public int getTime() {
        return time;
    }

    public int getDelay() {
        return delay;
    }

    public float getDistance() {
        return distance;
    }

    public float getKnockback() {
        return knockback;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public float getAngle() {
        return angle;
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
        private final Melee projectile;

        private Builder() {
            this.projectile = new Melee();
        }

        private Builder(Melee projectile) {
            this.projectile = projectile.copy();
        }

        public static Melee.Builder create() {
            return new Melee.Builder();
        }

        public static Melee.Builder create(Melee projectile) {
            return new Melee.Builder(projectile);
        }

        public Melee build() {
            return this.projectile.copy(); //Copy since the builder could be used again
        }

        public Melee.Builder setDamage(ResourceLocation id, float damage) {
            this.projectile.damage = damage;
            return this;
        }

        public Melee.Builder setDistance(float distance) {
            this.projectile.distance = distance;
            return this;
        }

        public Melee.Builder setKnockback(float knockback) {
            this.projectile.knockback = knockback;
            return this;
        }

        public Melee.Builder setMaxTargets(int maxTargets) {
            this.projectile.maxTargets = maxTargets;
            return this;
        }

        public Melee.Builder setAttackRadius(float attackRadius) {
            this.projectile.angle = attackRadius;
            return this;
        }
    }
}

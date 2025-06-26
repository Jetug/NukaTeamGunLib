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
    private float damage = 0;
    private int time = 0;
    private int delay = 0;
    private int distance = 0;
    private int knockback = 0;

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat("Damage", this.damage);
        tag.putInt("time", this.time);
        tag.putInt("delay", this.delay);
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
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        var object = new JsonObject();
        object.addProperty("damage", this.damage);
        object.addProperty("time", this.time);
        object.addProperty("delay", this.delay);
        return object;
    }

    public Melee copy() {
        var projectile = new Melee();
        projectile.damage = this.damage;
        projectile.time = this.time;
        projectile.delay = this.delay;

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
    }
}

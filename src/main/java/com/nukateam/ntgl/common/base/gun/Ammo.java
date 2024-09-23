package com.nukateam.ntgl.common.base.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.nukateam.ntgl.client.ClientHandler.createEditorScreen;

public class Ammo implements INBTSerializable<CompoundTag>, IEditorMenu {
    private ResourceLocation item = new ResourceLocation(Ntgl.MOD_ID, "round10mm");
    @Optional
    private boolean visible;
    private float damage;
    private float size;
    private double speed;
    private int life;
    @Optional
    private boolean gravity;
    @Optional
    private boolean damageReduceOverLife;
    @Optional
    private boolean magazineMode;
    @Optional
    private int trailColor = 0xFFD289;
    @Optional
    private double trailLengthMultiplier = 1.0;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Item", this.item.toString());
        tag.putBoolean("Visible", this.visible);
        tag.putFloat("Damage", this.damage);
        tag.putFloat("Size", this.size);
        tag.putDouble("Speed", this.speed);
        tag.putInt("Life", this.life);
        tag.putBoolean("Gravity", this.gravity);
        tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
        tag.putBoolean("MagazineMode", this.magazineMode);
        tag.putInt("TrailColor", this.trailColor);
        tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Item", Tag.TAG_STRING)) {
            this.item = new ResourceLocation(tag.getString("Item"));
        }
        if (tag.contains("Visible", Tag.TAG_ANY_NUMERIC)) {
            this.visible = tag.getBoolean("Visible");
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
    }

    public Ammo copy() {
        var projectile = new Ammo();
        projectile.item = this.item;
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
        return projectile;
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        Preconditions.checkArgument(this.size >= 0.0F, "Projectile size must be more than or equal to zero");
        Preconditions.checkArgument(this.speed >= 0.0, "Projectile speed must be more than or equal to zero");
        Preconditions.checkArgument(this.life > 0, "Projectile life must be more than zero");
        Preconditions.checkArgument(this.trailLengthMultiplier >= 0.0, "Projectile trail length multiplier must be more than or equal to zero");
        JsonObject object = new JsonObject();
        object.addProperty("item", this.item.toString());
        if (this.visible) object.addProperty("visible", true);
        object.addProperty("damage", this.damage);
        object.addProperty("size", this.size);
        object.addProperty("speed", this.speed);
        object.addProperty("life", this.life);
        if (this.gravity) object.addProperty("gravity", true);
        if (this.damageReduceOverLife) object.addProperty("damageReduceOverLife", this.damageReduceOverLife);
        if (this.magazineMode) object.addProperty("magazineMode", this.magazineMode);
        if (this.trailColor != 0xFFD289) object.addProperty("trailColor", this.trailColor);
        if (this.trailLengthMultiplier != 1.0)
            object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
        return object;
    }

    /**
     * @return The registry id of the ammo item
     */
    public ResourceLocation getItem() {
        return this.item;
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
     * @return If the damage should reduce the further the projectile travels
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
     * @return The multiplier to change the length of the projectile trail
     */
    public double getTrailLengthMultiplier() {
        return this.trailLengthMultiplier;
    }

    public static Ammo create(CompoundTag tag) {
        var gun = new Ammo();
        gun.deserializeNBT(tag);
        return gun;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Ammo");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
//            ItemStack scope = Ammo.getScopeStack(heldItem);
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
}

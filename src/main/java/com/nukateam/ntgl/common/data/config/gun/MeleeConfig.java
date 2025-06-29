package com.nukateam.ntgl.common.data.config.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.util.GunJsonUtil;
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

public class MeleeConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String DAMAGE = "Damage";
    public static final String MELEE = "melee";

    private float damage = 1;
    private MeleeGeneral melee = new MeleeGeneral();


    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putFloat(DAMAGE, this.damage);
        tag.put(MELEE, this.melee.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            this.damage = tag.getFloat(DAMAGE);
        }
        if (tag.contains(MELEE, Tag.TAG_COMPOUND)) {
            this.melee = MeleeGeneral.create(tag.getCompound(MELEE));
        }

    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
        var object = new JsonObject();
        object.addProperty("damage", this.damage);
        GunJsonUtil.addObjectIfNotEmpty(object,"melee", this.melee.toJsonObject());
        return object;
    }

    public MeleeConfig copy() {
        var projectile = new MeleeConfig();
        projectile.damage = this.damage;
        return projectile;
    }

    /**
     * @return The damage caused by melee attack
     */
    public float getDamage() {
        return this.damage;
    }

    public MeleeGeneral getMelee() {
        return melee;
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
        private final MeleeConfig projectile;

        private Builder() {
            this.projectile = new MeleeConfig();
        }

        private Builder(MeleeConfig projectile) {
            this.projectile = projectile.copy();
        }

        public static MeleeConfig.Builder create() {
            return new MeleeConfig.Builder();
        }

        public static MeleeConfig.Builder create(MeleeConfig projectile) {
            return new MeleeConfig.Builder(projectile);
        }

        public MeleeConfig build() {
            return this.projectile.copy(); //Copy since the builder could be used again
        }

        public MeleeConfig.Builder setDamage(ResourceLocation id, float damage) {
            this.projectile.damage = damage;
            return this;
        }
    }
}

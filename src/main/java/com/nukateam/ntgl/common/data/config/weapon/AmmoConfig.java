
package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.CounterType;
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

public class AmmoConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    @Optional private AmmoType type = AmmoType.STANDARD;
    @Optional private CounterType counter = CounterType.NUMBER;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("type", this.type.toString());
        tag.putString("counter", this.counter.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("type", Tag.TAG_STRING)) {
            this.type = AmmoType.getType(tag.getString("type"));
        }
        if (tag.contains("counter", Tag.TAG_STRING)) {
            this.counter = CounterType.getType(tag.getString("counter"));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.addProperty("type", this.type.toString());
        object.addProperty("counter", this.counter.toString());
        return object;
    }

    public AmmoConfig copy() {
        var config = new AmmoConfig();
        config.type = this.type;
        config.counter = this.counter;

        return config;
    }

    public AmmoType getAmmoType() {
        return type;
    }

    public CounterType getCounter() {
        return counter;
    }

    public static AmmoConfig create(CompoundTag tag) {
        var ammo = new AmmoConfig();
        ammo.deserializeNBT(tag);
        return ammo;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Ammo");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {});
    }

    public static class Builder {
        private final AmmoConfig ammo;

        private Builder() {
            this.ammo = new AmmoConfig();
        }

        private Builder(AmmoConfig ammo) {
            this.ammo = ammo.copy();
        }

        public static AmmoConfig.Builder create() {
            return new AmmoConfig.Builder();
        }

        public static AmmoConfig.Builder create(AmmoConfig ammo) {
            return new AmmoConfig.Builder(ammo);
        }

        public AmmoConfig build() {
            return this.ammo.copy(); //Copy since the builder could be used again
        }
    }
}

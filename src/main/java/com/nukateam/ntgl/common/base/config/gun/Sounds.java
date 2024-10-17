package com.nukateam.ntgl.common.base.config.gun;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class Sounds implements INBTSerializable<CompoundTag> {
    @Optional
    @Nullable
    ResourceLocation fire;
    @Optional
    @Nullable
    ResourceLocation reload;
    @Optional
    @Nullable
    ResourceLocation cock;
    @Optional
    @Nullable
    ResourceLocation silencedFire;
    @Optional
    @Nullable
    ResourceLocation enchantedFire;
    @Optional
    @Nullable
    ResourceLocation preFire;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.fire != null) {
            tag.putString("Fire", this.fire.toString());
        }
        if (this.reload != null) {
            tag.putString("Reload", this.reload.toString());
        }
        if (this.cock != null) {
            tag.putString("Cock", this.cock.toString());
        }
        if (this.silencedFire != null) {
            tag.putString("SilencedFire", this.silencedFire.toString());
        }
        if (this.enchantedFire != null) {
            tag.putString("EnchantedFire", this.enchantedFire.toString());
        }
        if (this.preFire != null) {
            tag.putString("PreFire", this.preFire.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Fire", Tag.TAG_STRING)) {
            this.fire = this.createSound(tag, "Fire");
        }
        if (tag.contains("Reload", Tag.TAG_STRING)) {
            this.reload = this.createSound(tag, "Reload");
        }
        if (tag.contains("Cock", Tag.TAG_STRING)) {
            this.cock = this.createSound(tag, "Cock");
        }
        if (tag.contains("SilencedFire", Tag.TAG_STRING)) {
            this.silencedFire = this.createSound(tag, "SilencedFire");
        }
        if (tag.contains("EnchantedFire", Tag.TAG_STRING)) {
            this.enchantedFire = this.createSound(tag, "EnchantedFire");
        }
        if (tag.contains("PreFire", Tag.TAG_STRING)) {
            this.preFire = this.createSound(tag, "PreFire");
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        if (this.fire != null) {
            object.addProperty("fire", this.fire.toString());
        }
        if (this.reload != null) {
            object.addProperty("reload", this.reload.toString());
        }
        if (this.cock != null) {
            object.addProperty("cock", this.cock.toString());
        }
        if (this.silencedFire != null) {
            object.addProperty("silencedFire", this.silencedFire.toString());
        }
        if (this.enchantedFire != null) {
            object.addProperty("enchantedFire", this.enchantedFire.toString());
        }
        if (this.preFire != null) {
            object.addProperty("preFire", this.preFire.toString());
        }
        return object;
    }

    public Sounds copy() {
        Sounds sounds = new Sounds();
        sounds.fire = this.fire;
        sounds.reload = this.reload;
        sounds.cock = this.cock;
        sounds.silencedFire = this.silencedFire;
        sounds.enchantedFire = this.enchantedFire;
        sounds.preFire = this.preFire;
        return sounds;
    }

    @Nullable
    private ResourceLocation createSound(CompoundTag tag, String key) {
        String sound = tag.getString(key);
        return sound.isEmpty() ? null : new ResourceLocation(sound);
    }

    /**
     * @return The registry id of the sound event when firing this weapon
     */
    @Nullable
    public ResourceLocation getFire() {
        return this.fire;
    }

    /**
     * @return The registry iid of the sound event when reloading this weapon
     */
    @Nullable
    public ResourceLocation getReload() {
        return this.reload;
    }

    /**
     * @return The registry iid of the sound event when cocking this weapon
     */
    @Nullable
    public ResourceLocation getCock() {
        return this.cock;
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getSilencedFire() {
        return this.silencedFire;
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getEnchantedFire() {
        return this.enchantedFire;
    }

    @Nullable
    public ResourceLocation getPreFire() {
        return this.preFire;
    }
}

package com.nukateam.ntgl.common.data.config.gun;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class Sounds{
    private final Gun gun;

    public Sounds(Gun gun) {
        this.gun = gun;
    }

    /**
     * @return The registry id of the sound event when firing this weapon
     */
    @Nullable
    public ResourceLocation getFire() {
        return gun.sounds.get("fire");
    }

    /**
     * @return The registry iid of the sound event when reloading this weapon
     */
    @Nullable
    public ResourceLocation getReload() {
        return gun.sounds.get("reload");
    }

    /**
     * @return The registry iid of the sound event when cocking this weapon
     */
    @Nullable
    public ResourceLocation getCock() {
        return gun.sounds.get("cock");
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getSilencedFire() {
        return gun.sounds.get("silencedFire");
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getEnchantedFire() {
        return gun.sounds.get("enchantedFire");
    }

    @Nullable
    public ResourceLocation getPreFire() {
        return gun.sounds.get("preFire");
    }
}

package com.nukateam.ntgl.common.data.config.weapon;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class Sounds{
    private final WeaponConfig weaponConfig;

    public enum SoundType{
        FIRE("fire"),
        SILENCED_FIRE("silencedFire"),
        ENCHANTED_FIRE("enchantedFire"),
        RELOAD("reload"),
        COCK("cock"),
        PRE_FIRE("preFire");

        private final String name;

        SoundType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public Sounds(WeaponConfig weaponConfig) {
        this.weaponConfig = weaponConfig;
    }

    /**
     * @return The registry id of the sound event when firing this weapon
     */
    @Nullable
    public ResourceLocation getFire() {
        return weaponConfig.sounds.get("fire");
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getSilencedFire() {
        return weaponConfig.sounds.get("silencedFire");
    }

    /**
     * @return The registry iid of the sound event when silenced firing this weapon
     */
    @Nullable
    public ResourceLocation getEnchantedFire() {
        return weaponConfig.sounds.get("enchantedFire");
    }
    /**
     * @return The registry iid of the sound event when reloading this weapon
     */
    @Nullable
    public ResourceLocation getReload() {
        return weaponConfig.sounds.get("reload");
    }

    /**
     * @return The registry iid of the sound event when cocking this weapon
     */
    @Nullable
    public ResourceLocation getCock() {
        return weaponConfig.sounds.get("cock");
    }


    @Nullable
    public ResourceLocation getPreFire() {
        return weaponConfig.sounds.get("preFire");
    }
}

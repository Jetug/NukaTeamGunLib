package com.nukateam.ntgl.common.foundation.event;

import com.nukateam.ntgl.common.data.WeaponData;
import net.neoforged.bus.api.Event;

public abstract class MeleeWeaponModifiersEvent extends Event {

    private final WeaponData weaponData;
    
    public MeleeWeaponModifiersEvent(WeaponData weaponData) {
        this.weaponData = weaponData;
    }

    public WeaponData getWeaponData() {
        return weaponData;
    }

    public static class Damage extends MeleeWeaponModifiersEvent {
        private float modifier;

        public Damage(WeaponData weaponData, float modifier) {
            super(weaponData);
            this.modifier = modifier;
        }

        public float getModifier() {
            return modifier;
        }

        public void setModifier(float modifier) {
            this.modifier = modifier;
        }
    }

    public static class Cooldown extends MeleeWeaponModifiersEvent {
        private int modifier;

        public Cooldown(WeaponData weaponData, int modifier) {
            super(weaponData);
            this.modifier = modifier;
        }

        public int getModifier() {
            return modifier;
        }

        public void setModifier(int modifier) {
            this.modifier = modifier;
        }
    }
}

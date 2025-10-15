package com.nukateam.example.common.registery;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.util.Mth;

import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class GunModifiers {
    public static final IGunModifier SILENCED = new IGunModifier() {
        @Override
        public boolean silencedFire(boolean value, WeaponData data) {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius, WeaponData data) {
            return radius * 0.25;
        }

        @Override
        public int modifyFireRate(int rate, WeaponData data) {
            return 1;
        }
    };

    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier() {
        @Override
        public float modifyDamage(float damage, WeaponData data) {
            return damage * 0.75F;
        }
    };

    public static final IGunModifier SLOW_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier SLOWER_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier EXTENDED_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo, WeaponData data) {
            return (int) (maxAmmo * 1.5);
        }
    };

    public static final IGunModifier DRUM_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo, WeaponData data) {
            return maxAmmo * 4;
        }
    };

    public static final IGunModifier BETTER_CONTROL = new IGunModifier() {
        @Override
        public float recoilModifier(WeaponData data) {
            return 0.3F;
        }

        @Override
        public float kickModifier(WeaponData data) {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread, WeaponData data) {
            return spread * 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier(WeaponData data) {
            return 0.4F;
        }

        @Override
        public float kickModifier(WeaponData data) {
            return 0.3F;
        }

        @Override
        public float modifyProjectileSpread(float spread, WeaponData data) {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.9F;
        }

        @Override
        public int modifyFireRate(int rate, WeaponData data) {
            return rate + 20;
        }
    };

    public static final IGunModifier SUPER_STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier(WeaponData data) {
            return 0.1F;
        }

        @Override
        public float kickModifier(WeaponData data) {
            return 0.1F;
        }

        @Override
        public float modifyProjectileSpread(float spread, WeaponData data) {
            return spread * 0.25F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.5F;
        }

        @Override
        public int modifyFireRate(int rate, WeaponData data) {
            return Mth.clamp((int) (rate * 1.25), rate + 1, Integer.MAX_VALUE);
        }
    };

    public static final IGunModifier LIGHT_RECOIL = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo, WeaponData data) {
            return (int)(maxAmmo * 2.5);
        }

        @Override
        public int modifyProjectileAmount(int amount, WeaponData data) {
            return IGunModifier.super.modifyProjectileAmount(amount, data);
        }

        @Override
        public Set<AmmoHolder> modifyAmmoItems(Set<AmmoHolder> item, WeaponData data) {
            return IGunModifier.super.modifyAmmoItems(item, data);
        }

        @Override
        public float recoilModifier(WeaponData data) {
            return 0.75F;
        }

        @Override
        public float kickModifier(WeaponData data) {
            return 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 1.2F;
        }

        @Override
        public float modifyProjectileSpread(float spread, WeaponData data) {
            return spread * 0.8F;
        }

        @Override
        public int modifyFireRate(int rate, WeaponData data) {
            return rate * 2;
        }

        @Override
        public float modifyDamage(float damage, WeaponData data) {
            return damage + 20;
        }
    };

    public static final IGunModifier REDUCED_RECOIL = new IGunModifier() {
        @Override
        public Set<FireMode> modifyFireModes(Set<FireMode> fireMode, WeaponData data) {
            return Set.of(FireMode.AUTO, FireMode.MULTI);
        }

        @Override
        public int modifyFireRate(int rate, WeaponData data) {
            return 15;
        }

        @Override
        public float recoilModifier(WeaponData data) {
            return 1.53F;
        }

        @Override
        public float kickModifier(WeaponData data) {
            return 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, WeaponData data) {
            return speed * 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread, WeaponData data) {
            return spread * 0.5F;
        }
    };
}

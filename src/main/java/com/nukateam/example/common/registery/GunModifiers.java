package com.nukateam.example.common.registery;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class GunModifiers {
    public static final IGunModifier SILENCED = new IGunModifier() {
        @Override
        public boolean silencedFire(GunData data) {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius, GunData data) {
            return radius * 0.25;
        }

        @Override
        public int modifyFireRate(int rate, GunData data) {
            return 1;
        }
    };

    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier() {
        @Override
        public float modifyDamage(float damage, GunData data) {
            return damage * 0.75F;
        }
    };

    public static final IGunModifier SLOW_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier SLOWER_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier EXTENDED_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo, GunData data) {
            return (int) (maxAmmo * 1.5);
        }
    };

    public static final IGunModifier DRUM_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo, GunData data) {
            return maxAmmo * 4;
        }
    };

    public static final IGunModifier BETTER_CONTROL = new IGunModifier() {
        @Override
        public float recoilModifier(GunData data) {
            return 0.3F;
        }

        @Override
        public float kickModifier(GunData data) {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread, GunData data) {
            return spread * 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier(GunData data) {
            return 0.4F;
        }

        @Override
        public float kickModifier(GunData data) {
            return 0.3F;
        }

        @Override
        public float modifyProjectileSpread(float spread, GunData data) {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier SUPER_STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier(GunData data) {
            return 0.1F;
        }

        @Override
        public float kickModifier(GunData data) {
            return 0.1F;
        }

        @Override
        public float modifyProjectileSpread(float spread, GunData data) {
            return spread * 0.25F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.5F;
        }

        @Override
        public int modifyFireRate(int rate, GunData data) {
            return Mth.clamp((int) (rate * 1.25), rate + 1, Integer.MAX_VALUE);
        }
    };

    public static final IGunModifier LIGHT_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier(GunData data) {
            return 0.75F;
        }

        @Override
        public float kickModifier(GunData data) {
            return 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 1.2F;
        }

        @Override
        public float modifyProjectileSpread(float spread, GunData data) {
            return spread * 0.8F;
        }
    };

    public static final IGunModifier REDUCED_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier(GunData data) {
            return 0.5F;
        }

        @Override
        public float kickModifier(GunData data) {
            return 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed, GunData data) {
            return speed * 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread, GunData data) {
            return spread * 0.5F;
        }
    };
}

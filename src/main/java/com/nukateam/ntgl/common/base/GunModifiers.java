package com.nukateam.ntgl.common.base;

import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GunModifiers {
    public static final IGunModifier SILENCED = new IGunModifier() {
        @Override
        public boolean silencedFire() {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius) {
            return radius * 0.25;
        }

        @Override
        public int modifyFireRate(int rate) {
            return 1;
        }
    };

    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier() {
        @Override
        public float modifyDamage(float damage) {
            return damage * 0.75F;
        }
    };

    public static final IGunModifier SLOW_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier SLOWER_ADS = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier EXTENDED_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo) {
            return (int) (maxAmmo * 1.5);
        }
    };

    public static final IGunModifier DRUM_MAG = new IGunModifier() {
        @Override
        public int modifyMaxAmmo(int maxAmmo) {
            return maxAmmo * 4;
        }
    };

    public static final IGunModifier BETTER_CONTROL = new IGunModifier() {
        @Override
        public float recoilModifier() {
            return 0.3F;
        }

        @Override
        public float kickModifier() {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier() {
            return 0.4F;
        }

        @Override
        public float kickModifier() {
            return 0.3F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier SUPER_STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier() {
            return 0.1F;
        }

        @Override
        public float kickModifier() {
            return 0.1F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.25F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.5F;
        }

        @Override
        public int modifyFireRate(int rate) {
            return Mth.clamp((int) (rate * 1.25), rate + 1, Integer.MAX_VALUE);
        }
    };

    public static final IGunModifier LIGHT_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier() {
            return 0.75F;
        }

        @Override
        public float kickModifier() {
            return 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 1.2F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.8F;
        }
    };

    public static final IGunModifier REDUCED_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier() {
            return 0.5F;
        }

        @Override
        public float kickModifier() {
            return 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.5F;
        }
    };
}

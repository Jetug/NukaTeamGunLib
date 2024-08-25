package com.nukateam.ntgl.common.data.constants;

import com.nukateam.ntgl.common.base.GunModifiers;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class Attachments {
    public static final Scope SHORT_SCOPE = Scope.builder().aimFovModifier(0.7F).reticleOffset(1.55F).viewFinderDistance(1.1).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope MEDIUM_SCOPE = Scope.builder().aimFovModifier(0.5F).reticleOffset(1.625F).viewFinderDistance(1.0).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope LONG_SCOPE = Scope.builder().aimFovModifier(0.25F).reticleOffset(1.4F).viewFinderDistance(1.4).modifiers(GunModifiers.SLOWER_ADS).build();

    public static final IGunModifier SILENCED = new IGunModifier() {
        @Override
        public boolean silencedFire() {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius) {
            return radius * 0.25;
        }
    };

    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier() {
        @Override
        public float modifyProjectileDamage(float damage) {
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

package com.nukateam.ntgl.common.data.config.gun;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.*;

import java.util.Set;

public interface IGeneral {
    Set<AmmoHolder> getAmmo();

    Set<AmmoHolder> getFuel();

    Set<FireMode> getFireModes();

    boolean isFullCharge();

    boolean isEnchantable();

    default boolean isSilenced(boolean silenced, GunData data) {
        return silenced;
    }

    int getRate();

    int getFireDelay();

    GripType getGripType();

    int getMaxAmmo();

    int getReloadAmount();

    int getReloadStart();

    int getReloadTime();

    int getReloadEnd();

    int getEquipTime();

    int getAmmoPerShot();

    WeaponMode getWeaponMode();

    LoadingType getLoadingType();

    boolean isAutoReloading();

    boolean shouldRenderHud();

    String getCategory();

    float getRecoilAngle();

    float getDamage();

    float getRecoilKick();

    float getRecoilDurationOffset();

    float getRecoilAdsReduction();

    int getProjectileAmount();

    int getMultishotAmount();

    boolean isAlwaysSpread();

    boolean isOneTimeCharge();

    boolean canMelee();

    float getSpread();

    float getMovementSpeed();
}

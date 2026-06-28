package com.nukateam.ntgl.client.util;

import net.minecraft.resources.ResourceLocation;

public class ClientDebug {
    public static int X = 0;
    public static int Y = 0;
    public static int Z = 0;

    public static int weaponX = 0;
    public static int weaponY = 0;
    public static int weaponZ = 0;

    public static int RX = 0;
    public static int RY = 0;
    public static int RZ = 0;

    public static int scopeX = 0;
    public static int scopeY = 0;
    public static int scopeZ = 0;

    public static int rArmX = 0;
    public static int rArmY = 0;
    public static int rArmZ = 0;

    public static int rArmRX = 0;
    public static int rArmRY = 0;
    public static int rArmRZ = 0;

    public static int lArmX = 0;
    public static int lArmY = 0;
    public static int lArmZ = 0;

    public static int lArmRX = 0;
    public static int lArmRY = 0;
    public static int lArmRZ = 0;

    public static int gRX = 0;
    public static int gRY = 0;
    public static int gRZ = 0;

    public static int hudX = 0;
    public static int hudY = 0;
    public static int hudZ = 0;

    public static int muzzleFlashX = 0;
    public static int muzzleFlashY = 0;
    public static int muzzleFlashZ = 0;

    // ============ GETTERS ============
    public static int getX() {
        switch (tuningMode) {
            case GENERIC -> { return X; }
            case WEAPON_POSITION -> { return weaponX; }
            case RIGHT_ARM -> { return rArmX; }
            case LEFT_ARM -> { return lArmX; }
            case WEAPON_SCOPE -> { return scopeX; }
            case GUN_HUD -> { return hudX; }
            case MUZZLE_FLASH -> { return muzzleFlashX; }
            default -> { return 0; }
        }
    }

    public static int getY() {
        switch (tuningMode) {
            case GENERIC -> { return Y; }
            case WEAPON_POSITION -> { return weaponY; }
            case RIGHT_ARM -> { return rArmY; }
            case LEFT_ARM -> { return lArmY; }
            case WEAPON_SCOPE -> { return scopeY; }
            case GUN_HUD -> { return hudY; }
            case MUZZLE_FLASH -> { return muzzleFlashY; }
            default -> { return 0; }
        }
    }

    public static int getZ() {
        switch (tuningMode) {
            case GENERIC -> { return Z; }
            case WEAPON_POSITION -> { return weaponZ; }
            case RIGHT_ARM -> { return rArmZ; }
            case LEFT_ARM -> { return lArmZ; }
            case WEAPON_SCOPE -> { return scopeZ; }
            case GUN_HUD -> { return hudZ; }
            case MUZZLE_FLASH -> { return muzzleFlashZ; }
            default -> { return 0; }
        }
    }

    public static int getRX() {
        switch (tuningMode) {
            case GENERIC -> { return RX; }
            case RIGHT_ARM -> { return rArmRX; }
            case LEFT_ARM -> { return lArmRX; }
            case WEAPON_SCOPE -> { return gRX; }
            default -> { return 0; }
        }
    }

    public static int getRY() {
        switch (tuningMode) {
            case GENERIC -> { return RY; }
            case RIGHT_ARM -> { return rArmRY; }
            case LEFT_ARM -> { return lArmRY; }
            case WEAPON_SCOPE -> { return gRY; }
            default -> { return 0; }
        }
    }

    public static int getRZ() {
        switch (tuningMode) {
            case GENERIC -> { return RZ; }
            case RIGHT_ARM -> { return rArmRZ; }
            case LEFT_ARM -> { return lArmRZ; }
            case WEAPON_SCOPE -> { return gRZ; }
            default -> { return 0; }
        }
    }

    // ============ SETTERS ============
    public static void setX(int value) {
        switch (tuningMode) {
            case GENERIC -> X = value;
            case WEAPON_POSITION -> weaponX = value;
            case RIGHT_ARM -> rArmX = value;
            case LEFT_ARM -> lArmX = value;
            case WEAPON_SCOPE -> scopeX = value;
            case GUN_HUD -> hudX = value;
            case MUZZLE_FLASH -> muzzleFlashX = value;
        }
    }

    public static void setY(int value) {
        switch (tuningMode) {
            case GENERIC -> Y = value;
            case WEAPON_POSITION -> weaponY = value;
            case RIGHT_ARM -> rArmY = value;
            case LEFT_ARM -> lArmY = value;
            case WEAPON_SCOPE -> scopeY = value;
            case GUN_HUD -> hudY = value;
            case MUZZLE_FLASH -> muzzleFlashY = value;
        }
    }

    public static void setZ(int value) {
        switch (tuningMode) {
            case GENERIC -> Z = value;
            case WEAPON_POSITION -> weaponZ = value;
            case RIGHT_ARM -> rArmZ = value;
            case LEFT_ARM -> lArmZ = value;
            case WEAPON_SCOPE -> scopeZ = value;
            case GUN_HUD -> hudZ = value;
            case MUZZLE_FLASH -> muzzleFlashZ = value;
        }
    }

    public static void setRX(int value) {
        switch (tuningMode) {
            case GENERIC -> RX = value;
            case RIGHT_ARM -> rArmRX = value;
            case LEFT_ARM -> lArmRX = value;
            case WEAPON_SCOPE -> gRX = value;
            default -> {}
        }
    }

    public static void setRY(int value) {
        switch (tuningMode) {
            case GENERIC -> RY = value;
            case RIGHT_ARM -> rArmRY = value;
            case LEFT_ARM -> lArmRY = value;
            case WEAPON_SCOPE -> gRY = value;
            default -> {}
        }
    }

    public static void setRZ(int value) {
        switch (tuningMode) {
            case GENERIC -> RZ = value;
            case RIGHT_ARM -> rArmRZ = value;
            case LEFT_ARM -> lArmRZ = value;
            case WEAPON_SCOPE -> gRZ = value;
            default -> {}
        }
    }

    public static void addX(int value) {
        setX(getX() + value);
    }

    public static void addY(int value) {
        setY(getY() + value);
    }

    public static void addZ(int value) {
        setZ(getZ() + value);
    }

    public static void addRX(int value) {
        setRX(getRX() + value);
    }

    public static void addRY(int value) {
        setRY(getRY() + value);
    }

    public static void addRZ(int value) {
        setRZ(getRZ() + value);
    }

    public static void resetCurrent() {
        setX(0);
        setY(0);
        setZ(0);
        setRX(0);
        setRY(0);
        setRZ(0);
    }

    public static TuningMode tuningMode = TuningMode.GENERIC;
    public static boolean isHidden = false;
    public static ResourceLocation currentlyTuningItem = null;
}

package com.nukateam.ntgl.client.util;

import net.minecraft.resources.ResourceLocation;

public class ClientDebug {
    public static int X = 0;
    public static int Y = 0;
    public static int Z = 0;

    public static int RX = 0;
    public static int RY = 0;
    public static int RZ = 0;
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

    public static int gX = 0;
    public static int gY = 0;
    public static int gZ = 0;

    public static int gRX = 0;
    public static int gRY = 0;
    public static int gRZ = 0;
    public static int grArmX = 0;
    public static int grArmY = 0;
    public static int grArmZ = 0;
    public static int grArmRX = 0;
    public static int grArmRY = 0;
    public static int grArmRZ = 0;
    public static int glArmX = 0;
    public static int glArmY = 0;
    public static int glArmZ = 0;
    public static int glArmRX = 0;
    public static int glArmRY = 0;
    public static int glArmRZ = 0;

    public static int hX = 0;
    public static int hY = 0;
    public static int hZ = 0;
    public static int hRX = 0;
    public static int hRY = 0;
    public static int hRZ = 0;

    public static int emX = 0;
    public static int emY = 0;
    public static int emZ = 0;

    public static int hudX = 0;
    public static int hudY = 0;
    public static int hudZ = 0;

    public static int gpX = 0;
    public static int gpY = 0;
    public static int gpZ = 0;

    public static int mfX = 0;
    public static int mfY = 0;
    public static int mfZ = 0;

    // ============ GETTERS ============
    public static int getX() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return X; }
            case UNIQUE_RIGHT_ARM -> { return rArmX; }
            case UNIQUE_LEFT_ARM -> { return lArmX; }
            case GLOBAL_WEAPON -> { return gX; }
            case GLOBAL_RIGHT_ARM -> { return grArmX; }
            case GLOBAL_LEFT_ARM -> { return glArmX; }
            case PASSENGER_HEAD -> { return hX; }
            case EMISSIVE_LAYER -> { return emX; }
            case GUN_HUD -> { return hudX; }
            case GRENADE_POSE -> { return gpX; }
            case MUZZLE_FLASH -> { return mfX; }
            default -> { return 0; }
        }
    }

    public static int getY() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return Y; }
            case UNIQUE_RIGHT_ARM -> { return rArmY; }
            case UNIQUE_LEFT_ARM -> { return lArmY; }
            case GLOBAL_WEAPON -> { return gY; }
            case GLOBAL_RIGHT_ARM -> { return grArmY; }
            case GLOBAL_LEFT_ARM -> { return glArmY; }
            case PASSENGER_HEAD -> { return hY; }
            case EMISSIVE_LAYER -> { return emY; }
            case GUN_HUD -> { return hudY; }
            case GRENADE_POSE -> { return gpY; }
            case MUZZLE_FLASH -> { return mfY; }
            default -> { return 0; }
        }
    }

    public static int getZ() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return Z; }
            case UNIQUE_RIGHT_ARM -> { return rArmZ; }
            case UNIQUE_LEFT_ARM -> { return lArmZ; }
            case GLOBAL_WEAPON -> { return gZ; }
            case GLOBAL_RIGHT_ARM -> { return grArmZ; }
            case GLOBAL_LEFT_ARM -> { return glArmZ; }
            case PASSENGER_HEAD -> { return hZ; }
            case EMISSIVE_LAYER -> { return emZ; }
            case GUN_HUD -> { return hudZ; }
            case GRENADE_POSE -> { return gpZ; }
            case MUZZLE_FLASH -> { return mfZ; }
            default -> { return 0; }
        }
    }

    public static int getRX() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return RX; }
            case UNIQUE_RIGHT_ARM -> { return rArmRX; }
            case UNIQUE_LEFT_ARM -> { return lArmRX; }
            case GLOBAL_WEAPON -> { return gRX; }
            case GLOBAL_RIGHT_ARM -> { return grArmRX; }
            case GLOBAL_LEFT_ARM -> { return glArmRX; }
            case PASSENGER_HEAD -> { return hRX; }
            default -> { return 0; }
        }
    }

    public static int getRY() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return RY; }
            case UNIQUE_RIGHT_ARM -> { return rArmRY; }
            case UNIQUE_LEFT_ARM -> { return lArmRY; }
            case GLOBAL_WEAPON -> { return gRY; }
            case GLOBAL_RIGHT_ARM -> { return grArmRY; }
            case GLOBAL_LEFT_ARM -> { return glArmRY; }
            case PASSENGER_HEAD -> { return hRY; }
            default -> { return 0; }
        }
    }

    public static int getRZ() {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> { return RZ; }
            case UNIQUE_RIGHT_ARM -> { return rArmRZ; }
            case UNIQUE_LEFT_ARM -> { return lArmRZ; }
            case GLOBAL_WEAPON -> { return gRZ; }
            case GLOBAL_RIGHT_ARM -> { return grArmRZ; }
            case GLOBAL_LEFT_ARM -> { return glArmRZ; }
            case PASSENGER_HEAD -> { return hRZ; }
            default -> { return 0; }
        }
    }

    // ============ SETTERS ============
    public static void setX(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> X = value;
            case UNIQUE_RIGHT_ARM -> rArmX = value;
            case UNIQUE_LEFT_ARM -> lArmX = value;
            case GLOBAL_WEAPON -> gX = value;
            case GLOBAL_RIGHT_ARM -> grArmX = value;
            case GLOBAL_LEFT_ARM -> glArmX = value;
            case PASSENGER_HEAD -> hX = value;
            case EMISSIVE_LAYER -> emX = value;
            case GUN_HUD -> hudX = value;
            case GRENADE_POSE -> gpX = value;
            case MUZZLE_FLASH -> mfX = value;
        }
    }

    public static void setY(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> Y = value;
            case UNIQUE_RIGHT_ARM -> rArmY = value;
            case UNIQUE_LEFT_ARM -> lArmY = value;
            case GLOBAL_WEAPON -> gY = value;
            case GLOBAL_RIGHT_ARM -> grArmY = value;
            case GLOBAL_LEFT_ARM -> glArmY = value;
            case PASSENGER_HEAD -> hY = value;
            case EMISSIVE_LAYER -> emY = value;
            case GUN_HUD -> hudY = value;
            case GRENADE_POSE -> gpY = value;
            case MUZZLE_FLASH -> mfY = value;
        }
    }

    public static void setZ(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> Z = value;
            case UNIQUE_RIGHT_ARM -> rArmZ = value;
            case UNIQUE_LEFT_ARM -> lArmZ = value;
            case GLOBAL_WEAPON -> gZ = value;
            case GLOBAL_RIGHT_ARM -> grArmZ = value;
            case GLOBAL_LEFT_ARM -> glArmZ = value;
            case PASSENGER_HEAD -> hZ = value;
            case EMISSIVE_LAYER -> emZ = value;
            case GUN_HUD -> hudZ = value;
            case GRENADE_POSE -> gpZ = value;
            case MUZZLE_FLASH -> mfZ = value;
        }
    }

    public static void setRX(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> RX = value;
            case UNIQUE_RIGHT_ARM -> rArmRX = value;
            case UNIQUE_LEFT_ARM -> lArmRX = value;
            case GLOBAL_WEAPON -> gRX = value;
            case GLOBAL_RIGHT_ARM -> grArmRX = value;
            case GLOBAL_LEFT_ARM -> glArmRX = value;
            case PASSENGER_HEAD -> hRX = value;
            default -> {}
        }
    }

    public static void setRY(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> RY = value;
            case UNIQUE_RIGHT_ARM -> rArmRY = value;
            case UNIQUE_LEFT_ARM -> lArmRY = value;
            case GLOBAL_WEAPON -> gRY = value;
            case GLOBAL_RIGHT_ARM -> grArmRY = value;
            case GLOBAL_LEFT_ARM -> glArmRY = value;
            case PASSENGER_HEAD -> hRY = value;
            default -> {}
        }
    }

    public static void setRZ(int value) {
        switch (tuningMode) {
            case UNIQUE_WEAPON -> RZ = value;
            case UNIQUE_RIGHT_ARM -> rArmRZ = value;
            case UNIQUE_LEFT_ARM -> lArmRZ = value;
            case GLOBAL_WEAPON -> gRZ = value;
            case GLOBAL_RIGHT_ARM -> grArmRZ = value;
            case GLOBAL_LEFT_ARM -> glArmRZ = value;
            case PASSENGER_HEAD -> hRZ = value;
            default -> {}
        }
    }

    // ============ ADDERS (используют set и get) ============
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

    public static TuningMode tuningMode = TuningMode.GLOBAL_WEAPON;
    public static boolean isHidden = false;
    public static ResourceLocation currentlyTuningItem = null;
}

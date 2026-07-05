package com.nukateam.ntgl.client.util;

public enum TuningMode {
    GENERIC("Generic"),
    WEAPON_POSITION("Weapon Position"),
    WEAPON_SCOPE("Weapon Scope"),
    MUZZLE_FLASH("Muzzle Flash"),
    RIGHT_ARM("Right Arm"),
    LEFT_ARM("Left Arm"),
    GUN_HUD("Gun HUD");

    private final String name;

    TuningMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

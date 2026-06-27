package com.nukateam.ntgl.client.util;

public enum TuningMode {
    UNIQUE_WEAPON("Unique Weapon"),
    UNIQUE_RIGHT_ARM("Unique Right Arm"),
    UNIQUE_LEFT_ARM("Unique Left Arm"),
    GLOBAL_WEAPON("Global Weapon"),
    GLOBAL_RIGHT_ARM("Global Right Arm"),
    GLOBAL_LEFT_ARM("Global Left Arm"),
    PASSENGER_HEAD("Passenger Head"),
    EMISSIVE_LAYER("Emissive Layer"),
    GUN_HUD("Gun HUD"),
    GRENADE_POSE("Grenade Pose"),
    MUZZLE_FLASH("Muzzle Flash");

    private final String name;

    TuningMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

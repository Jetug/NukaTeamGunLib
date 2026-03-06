package com.nukateam.ntgl.common.data.enums;

public enum SoundType {
    FIRE("fire"),
    SILENCED_FIRE("silencedFire"),
    ENCHANTED_FIRE("enchantedFire"),
    RELOAD("reload"),
    COCK("cock"),
    PRE_FIRE("preFire");

    private final String name;

    SoundType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

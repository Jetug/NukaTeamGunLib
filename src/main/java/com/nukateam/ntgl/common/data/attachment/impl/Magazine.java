package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;

public class Magazine extends Attachment {
    private final float maxAmmo;

    public Magazine(int maxAmmo, IWeaponModifier... modifier) {
        super(modifier);
        this.maxAmmo = maxAmmo;
    }

    public float getMaxAmmo() {
        return this.maxAmmo;
    }

    public static Magazine create(int maxAmmo, IWeaponModifier... modifiers) {
        return new Magazine(maxAmmo, modifiers);
    }
}

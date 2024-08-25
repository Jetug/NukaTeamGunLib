package com.nukateam.ntgl.common.foundation.item.attachment.impl;

import com.nukateam.ntgl.common.data.interfaces.IGunModifier;

public class Magazine extends Attachment {
    private final float maxAmmo;

    private Magazine(int maxAmmo, IGunModifier... modifier) {
        super(modifier);
        this.maxAmmo = maxAmmo;
    }

    public float getMaxAmmo() {
        return this.maxAmmo;
    }

    public static Magazine create(int maxAmmo, IGunModifier... modifiers) {
        return new Magazine(maxAmmo, modifiers);
    }
}

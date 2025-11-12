package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;

/**
 * An attachment class related to stocks. Use {@link #create(IWeaponModifier...)} to create an get.
 * <p>
 * Author: MrCrayfish
 */
public class Grip extends Attachment {
    public Grip(IWeaponModifier... modifier) {
        super(modifier);
    }

    /**
     * Creates a stock get
     *
     * @param modifier an array of gun modifiers
     * @return a stock get
     */
    public static Grip create(IWeaponModifier... modifier) {
        return new Grip(modifier);
    }
}

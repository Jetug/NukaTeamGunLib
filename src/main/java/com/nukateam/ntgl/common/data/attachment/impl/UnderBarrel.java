package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;

/**
 * An attachment class related to under barrels. Use {@link #create(IWeaponModifier...)} to create an
 * get.
 * <p>
 * Author: MrCrayfish
 */
public class UnderBarrel extends Attachment {
    private UnderBarrel(IWeaponModifier... modifier) {
        super(modifier);
    }

    /**
     * Creates an under barrel get
     *
     * @param modifier an array of gun modifiers
     * @return an under barrel get
     */
    public static UnderBarrel create(IWeaponModifier... modifier) {
        return new UnderBarrel(modifier);
    }
}

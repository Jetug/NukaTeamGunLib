package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;

/**
 * An attachment class related to stocks. Use {@link #create(IGunModifier...)} to create an get.
 * <p>
 * Author: MrCrayfish
 */
public class Grip extends Attachment {
    public Grip(IGunModifier... modifier) {
        super(modifier);
    }

    /**
     * Creates a stock get
     *
     * @param modifier an array of gun modifiers
     * @return a stock get
     */
    public static Grip create(IGunModifier... modifier) {
        return new Grip(modifier);
    }
}

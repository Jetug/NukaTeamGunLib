package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;

public class GenericAttachment extends Attachment {
    private GenericAttachment(IGunModifier... modifier) {
        super(modifier);
    }

    /**
     * Creates generic attachment
     *
     * @param modifiers an array of gun modifiers
     * @return a barrel get
     */
    public static GenericAttachment create(IGunModifier... modifiers) {
        return new GenericAttachment(modifiers);
    }
}

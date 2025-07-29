package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.GenericAttachment;

public class GenericAttachmentItem extends AttachmentItem<GenericAttachment>{
    public GenericAttachmentItem(Properties properties) {
        super(AttachmentType.NONE, GenericAttachment.create(), properties);
    }
}

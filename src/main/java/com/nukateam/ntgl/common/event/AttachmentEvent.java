package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.util.util.GunData;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class AttachmentEvent extends Event {
    private final GunData gunData;

    public AttachmentEvent(GunData gunData) {
        this.gunData = gunData;
    }

    public GunData getGunData() {
        return gunData;
    }
//    private final Action action;
//    private final IAttachment<?> attachment;
//
//    public AttachmentEvent(Action action, IAttachment<?> attachment) {
//        this.action = action;
//        this.attachment = attachment;
//    }

//    public Action getAction() {
//        return action;
//    }
//
//    public IAttachment<?> getAttachment() {
//        return attachment;
//    }
//
//    public enum Action{
//        ADDING,
//        REMOVING
//    }
}

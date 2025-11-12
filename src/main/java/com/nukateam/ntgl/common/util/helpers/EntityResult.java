package com.nukateam.ntgl.common.util.helpers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityResult {
    private final Entity entity;
    private final Vec3 hitVec;
    private final boolean headshot;

    public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
        this.entity = entity;
        this.hitVec = hitVec;
        this.headshot = headshot;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Vec3 getHitPos() {
        return this.hitVec;
    }

    public boolean isHeadshot() {
        return this.headshot;
    }
}
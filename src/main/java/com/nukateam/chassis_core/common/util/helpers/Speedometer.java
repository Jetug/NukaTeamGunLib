package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static com.nukateam.chassis_core.common.util.helpers.VectorHelper.calculateDistance;

public class Speedometer {
    private final Entity entity;
    private double speed = 0;
    private Vec3 previousPosition;

    public Speedometer(Entity entity) {
        this.entity = entity;
        previousPosition = entity.position();
    }

    public void tick() {
        speed = calculateDistance(previousPosition, entity.position());
        previousPosition = entity.position();
    }

    public double getSpeed() {
        return speed;
    }
}

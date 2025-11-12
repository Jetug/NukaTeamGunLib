package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.world.phys.Vec3;

import static java.lang.Math.*;

public class VectorHelper {

    public static double calculateDistance(Vec3 v1, Vec3 v2) {
        return sqrt(pow(v1.x - v2.x, 2) + pow(v1.y - v2.y, 2) + pow(v1.z - v2.z, 2));
    }

    public static Vec3 getDirection(Vec3 v1, Vec3 v2) {
        return new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
    }

//    public static Vec3 viewRotationToVector(float rotation){
//
//    }

    public static Vec3 rotateVector(Vec3 v1, double degrees) {

        double angle = degrees;// toRadians(degrees);
        double rotatedX = v1.x * cos(angle) - v1.z * sin(angle);
        double rotatedZ = v1.x * sin(angle) + v1.z * cos(angle);

        return new Vec3(rotatedX, 0, rotatedZ);
    }
}

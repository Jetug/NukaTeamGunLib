package com.nukateam.chassis_core.common.util;

public class GlobalMixinData {
    public static BobType CURRENT = BobType.NONE;
    public enum BobType {
        HAND, CAMERA, NONE;
    }

}
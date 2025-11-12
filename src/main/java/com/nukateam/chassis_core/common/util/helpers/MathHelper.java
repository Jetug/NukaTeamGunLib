package com.nukateam.chassis_core.common.util.helpers;

public class MathHelper {
    public static double getFraction(double value, double maxValue) {
        return (value / maxValue);
    }

    public static int getInPercents(int value, int maxValue) {
        return (int) ((float) value / maxValue * 100);
    }

    public static float getPercentOf(int value, int percents) {
        return value * ((float) percents / 100.0f);
    }
}

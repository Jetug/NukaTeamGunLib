package com.nukateam.ntgl.client.util.util;

import com.nukateam.ntgl.client.render.hud.GunHud;

public class RgbUtils {
    public static int toRgba(int rgb){
        int alpha = 0xFF;
        return (alpha << 24) | rgb;
    }

    public static float[] rgbToFloatRgba(int rgb) {
        float red   = ((rgb >> GunHud.ICON_SIZE) & 0xFF) / 255.0f;
        float green = ((rgb >> 8)  & 0xFF) / 255.0f;
        float blue  =  (rgb        & 0xFF) / 255.0f;

        return new float[] {red, green, blue, 1}; // Returns [R, G, B, A]
    }
}

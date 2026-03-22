package com.nukateam.ntgl.common.util.data;

public class RGB {
    public final float red;
    public final float green;
    public final float blue;

    public RGB(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB(int color) {
        int redInt = (color >> 16) & 0xFF;
        int greenInt = (color >> 8) & 0xFF;
        int blueInt = color & 0xFF;

        red = redInt / 255.0f;
        green = greenInt / 255.0f;
        blue = blueInt / 255.0f;
    }

    public Rgba toRgba() {
        return new Rgba(red, green, blue, 1);
    }
}

package com.nukateam.ntgl.common.util.data;

import java.util.Objects;

public final class Rgba {
    public static Rgba DEFAULT = new Rgba(1.0f, 1.0f, 1.0f, 1.0f);
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public Rgba(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Rgba setAlpha(float a) {
        return new Rgba(this.r, this.g, this.b, a);
    }

    public int getIntColor() {
        int alpha = (int) (a * 255) & 0xFF;
        int red = (int) (r * 255) & 0xFF;
        int green = (int) (g * 255) & 0xFF;
        int blue = (int) (b * 255) & 0xFF;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public float a() {
        return a;
    }
}
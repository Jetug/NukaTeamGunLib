package com.nukateam.ntgl.common.util.data;

public record Rgba(float r, float g, float b, float a)
{
    public static Rgba DEFAULT = new Rgba(1.0f, 1.0f, 1.0f, 1.0f);

    public Rgba setAlpha(float a){
        return new Rgba(this.r, this.g, this.b, a);
    }
}
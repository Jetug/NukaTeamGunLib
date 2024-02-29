package com.nukateam.gunscore.common.data.util;

public record Rgba(float r, float g, float b, float a)
{
    public Rgba setAlpha(float a){
        return new Rgba(this.r, this.g, this.b, a);
    }
}
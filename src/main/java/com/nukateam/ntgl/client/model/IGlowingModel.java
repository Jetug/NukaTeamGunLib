package com.nukateam.ntgl.client.model;

import software.bernie.geckolib.animatable.GeoAnimatable;
import net.minecraft.resources.ResourceLocation;

public interface IGlowingModel<T extends GeoAnimatable> {
    ResourceLocation getGlowingTextureResource(T animatable);
}

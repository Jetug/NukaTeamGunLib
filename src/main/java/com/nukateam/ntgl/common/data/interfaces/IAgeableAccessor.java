package com.nukateam.ntgl.common.data.interfaces;

import net.minecraft.client.model.geom.ModelPart;

public interface IAgeableAccessor {
    boolean isScaleHead();

    float getBabyYHeadOffset();

    float getBabyZHeadOffset();

    float getBabyHeadScale();

    float getBabyBodyScale();

    float getBodyYOffset();

    Iterable<ModelPart> getHeadParts();

    Iterable<ModelPart> getBodyParts();
}

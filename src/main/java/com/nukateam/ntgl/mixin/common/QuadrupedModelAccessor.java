package com.nukateam.ntgl.mixin.common;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(QuadrupedModel.class)
public class QuadrupedModelAccessor {
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart leftFrontLeg;
}

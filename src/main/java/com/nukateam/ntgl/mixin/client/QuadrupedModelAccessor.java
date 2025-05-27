package com.nukateam.ntgl.mixin.client;

import com.nukateam.ntgl.common.util.interfaces.IModelAccessor;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(QuadrupedModel.class)
public class QuadrupedModelAccessor implements IModelAccessor {
    @Shadow protected ModelPart head;
    @Shadow protected ModelPart body;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightFrontLeg;
    @Shadow protected ModelPart leftFrontLeg;

    public List<ModelPart> getModelParts(){
        return List.of(head, body, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg);
    }
}

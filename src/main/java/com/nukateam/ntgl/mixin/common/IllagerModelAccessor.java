package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(IllagerModel.class)
public class IllagerModelAccessor implements IModelAccessor {
    @Shadow protected ModelPart head;
    @Shadow protected ModelPart hat;
    @Shadow protected ModelPart arms;
    @Shadow protected ModelPart leftLeg;
    @Shadow protected ModelPart rightLeg;
    @Shadow protected ModelPart rightArm;
    @Shadow protected ModelPart leftArm;

    public List<ModelPart> getModelParts(){
        return List.of(head, hat, arms, leftLeg, rightLeg, rightArm, leftArm);
    }
}

package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(VillagerModel.class)
public class VillagerModelAccessor implements IModelAccessor {
    @Shadow protected ModelPart head;
    @Shadow protected ModelPart hat;
    @Shadow protected ModelPart hatRim;
    @Shadow protected ModelPart rightLeg;
    @Shadow protected ModelPart leftLeg;
    @Shadow protected ModelPart nose;

    public List<ModelPart> getModelParts(){
        return List.of(head, hat, hatRim, rightLeg, leftLeg, nose);
    }
}

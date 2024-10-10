package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.data.interfaces.IAgeableAccessor;
import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelAccessor implements IAgeableAccessor, IModelAccessor {
    @Shadow @Final private boolean scaleHead;
    @Shadow @Final private float babyYHeadOffset;
    @Shadow @Final private float babyZHeadOffset;
    @Shadow @Final private float babyHeadScale;
    @Shadow @Final private float babyBodyScale;
    @Shadow @Final private float bodyYOffset;

    @Shadow protected abstract Iterable<ModelPart> headParts();
    @Shadow protected abstract Iterable<ModelPart> bodyParts();

    @Override public Iterable<ModelPart> getHeadParts(){
        return headParts();
    }

    @Override public Iterable<ModelPart> getBodyParts(){
        return bodyParts();
    }

    @Override
    public boolean isScaleHead() {
        return scaleHead;
    }

    @Override
    public float getBabyYHeadOffset() {
        return babyYHeadOffset;
    }

    @Override
    public float getBabyZHeadOffset() {
        return babyZHeadOffset;
    }

    @Override
    public float getBabyHeadScale() {
        return babyHeadScale;
    }

    @Override
    public float getBabyBodyScale() {
        return babyBodyScale;
    }

    @Override
    public float getBodyYOffset() {
        return bodyYOffset;
    }

    @Override
    public List<ModelPart> getModelParts() {
        var result = new ArrayList<ModelPart>();

        headParts().forEach(result::add);
        bodyParts().forEach(result::add);

        return result;
    }
}

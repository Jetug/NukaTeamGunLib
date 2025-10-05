package com.nukateam.chassis_core.client.model;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.chassis_core.client.render.utils.GeoUtils.setHeadAnimation;
import static com.nukateam.chassis_core.client.render.utils.ResourceHelper.getChassisResource;
import static com.nukateam.chassis_core.common.data.constants.Resources.resourceLocation;


@SuppressWarnings({"rawtypes", "unchecked"})
public class ChassisModel<Type extends WearableChassis> extends GeoModel<Type> {
    public ChassisModel() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(Type object) {
        if (object == null) return resourceLocation("geo/error.geo.json");
        return getChassisResource(object, "geo/chassis/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Type object) {
        if (object == null) return resourceLocation("textures/entity/error.png");
        return getChassisResource(object, "textures/entity/", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(Type object) {
        if (object == null) return resourceLocation("animations/error.animation.json");
        return getChassisResource(object, "animations/", ".animation.json");
    }

    @Override
    public void setCustomAnimations(Type animatable, long instanceId, AnimationState<Type> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (animatable.hasPassenger())
            setHeadAnimation(animatable, this.getAnimationProcessor(), animationState);
    }

//    @Override
//    public void setCustomAnimations(Type animatable, int instanceId, AnimationEvent animationEvent) {
//        super.setCustomAnimations(animatable, instanceId, animationEvent);
//        var head = this.getAnimationProcessor().getBone(HEAD_BONE_NAME);
//        if(head == null) return;
//        var extraData = (EntityModelData) animationEvent.getExtraDataOfType(EntityModelData.class).get(0);
//        head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
//        head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
//    }
}
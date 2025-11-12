package com.nukateam.chassis_core.client.render.utils;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"rawtypes"})
public class GeoUtils {
    @Nullable
    public static Collection<GeoBone> getEquipmentBones(String boneName, WearableChassis animatable) {
        var result = new ArrayList<GeoBone>();
        var configs = animatable.getItemConfigs();
        for (var config : configs) {
            var boneNames = config.getArmorBone(boneName);

            for (var name : boneNames) {
                var armorBone = GeoUtils.getBone(config.getModel(), name);
                if (armorBone != null) result.add(armorBone);
            }
        }
        return result;
    }


    public static void setHeadAnimation(GeoBone head, AnimationState animationState) {
        if (head == null) return;
        var data = (EntityModelData) animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(data.headPitch() * ((float) Math.PI / 180F));
        head.setRotY(data.netHeadYaw() * ((float) Math.PI / 180F));
    }

    public static void setHeadAnimation(LivingEntity animatable, AnimationProcessor animationProcessor, AnimationState animationState) {
        var head = animationProcessor.getBone("head");
        if (head == null) return;
        setHeadAnimation(head, animationState);
    }

    public static BakedGeoModel getModel(ResourceLocation location) {
        return GeckoLibCache.getBakedModels().get(location);
    }

    @Nullable
    public static GeoBone getBone(ResourceLocation resourceLocation, String name) {
        var model = getModel(resourceLocation);
        return model == null ? null : model.getBone(name).orElse(null);
    }

    public static Vec3 getRot(GeoBone bone){
        return new Vec3(bone.getRotX(), bone.getRotY(), bone.getRotZ());
    }
    public static Vec3 getPos(GeoBone bone){
        return new Vec3(bone.getPosX(), bone.getPosY(), bone.getPosZ());
    }

    public static void setRot(GeoBone bone, Vec3 pos){
        bone.setRotX((float) pos.x);
        bone.setRotX((float) pos.y);
        bone.setRotX((float) pos.z);
    }

    public static void setPos(GeoBone bone, Vec3 pos){
        bone.setPosX((float) pos.x);
        bone.setPosY((float) pos.y);
        bone.setPosZ((float) pos.z);
    }

    public static @Nullable ResourceLocation getTextureForBone(GeoBone bone, WearableChassis animatable) {
        if(bone == null || animatable == null) return null;

        var texture = animatable.getTextureForBone(bone.getName());

        if(texture == null){
            var parent =  bone.getParent();
            while (parent != null && texture == null){
                texture = animatable.getTextureForBone(parent.getName());
                parent =  parent.getParent();
            }
        }

        return texture;
    }
}

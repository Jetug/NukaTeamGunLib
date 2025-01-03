package com.nukateam.ntgl.client.render.renderers.misc;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.ModelGibsAgeable;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeneric;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeo;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DeathFxRenderer {
    private static DeathEffect.GoreData genericGore;

    static {
        genericGore = (new DeathEffect.GoreData(null, 160, 21, 31))
                .setTexture(new ResourceLocation(Ntgl.MOD_ID, "textures/entity/gore.png"));
        genericGore.setRandomScale(0.5f, 0.8f);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setupGoreData(LivingEntity entity, DeathEffect.GoreData data) {
        var render = ClientProxy.getEntityRenderer(entity);

        if (data.model == null) {
            if (render instanceof GeoEntityRenderer geoRenderer && entity instanceof GeoAnimatable animatable) {
                var geoModel = geoRenderer.getGeoModel();
                var model = geoModel.getBakedModel(geoModel.getModelResource(animatable));
                data.model = new ModelGibsGeo(model, geoRenderer);
            } else if (render instanceof LivingEntityRenderer livingRenderer) {
                var mainModel = livingRenderer.getModel();

                if (mainModel instanceof HierarchicalModel<? extends Entity> model) {
                    data.model = new ModelGibsGeneric(model);
                } else if (mainModel instanceof AgeableListModel<? extends Entity> model) {
                    data.model = new ModelGibsAgeable(model);
                } else {
                    data.model = genericGore.model;
                    data.texture = genericGore.texture;
                }
            }
        }
    }
}

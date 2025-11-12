package com.nukateam.ntgl.client.render.renderers.misc;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.ModelGibsAgeable;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeneric;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeo;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.entity.projectile.GoreData;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static com.nukateam.ntgl.ClientProxy.*;

@OnlyIn(Dist.CLIENT)
public class DeathFxRenderer {
    private static final ResourceLocation RES_BURN_EFFECT = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/fx/death/burn.png");
    private static final ResourceLocation RES_LASER_EFFECT = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/fx/death/laser.png");

    private static GoreData genericGore;
    static {
        genericGore = (new GoreData(null, 160, 21, 31))
                .setTexture(ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/entity/gore.png"));
        genericGore.setRandomScale(0.5f, 0.8f);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setupGoreData(LivingEntity entity, GoreData data) {
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

    @OnlyIn(Dist.CLIENT)
    public static void addClientEntity(FlyingGib entity) {
        try {
            var level = Minecraft.getInstance().level;
            var mtd = level.getClass().getDeclaredMethod("addEntity", int.class, Entity.class);
            mtd.setAccessible(true);
            mtd.invoke(level, level.random.nextInt(Integer.MAX_VALUE), entity);
        }
        catch (Exception e){
            Ntgl.LOGGER.error("reflection fail", e);
        }
    }

    public static void createDeathEffectClient(LivingEntity entity, GoreData data) {
        double x = entity.getX();
        double y = entity.getY() + (entity.getType().getHeight() / 2.0f);
        double z = entity.getZ();
        var vec = new Vec3(x, y, z);

        setDamageType(entity, data.deathType);

        switch (data.deathType){
            case GORE -> {
                setupGoreData(entity, data);
                data.gravity = 0.2f;
                createGoreFx(entity, vec, data);
            }
            case LASER -> {
                setupGoreData(entity, data);
                data.showBlood = false;
                data.gravity = 110.2f;
                createDisintegrationFx(entity, vec, data);
            }
            case FIRE -> {
                setupGoreData(entity, data);
                data.showBlood = false;
                data.gravity = 110.2f;
                createBurnFx(entity, vec, data);
            }
        }
    }

    private static void createGoreFx(LivingEntity entity, Vec3 vec, GoreData data) {
//        var minecraft = Minecraft.getInstance();
//        minecraft.level.playSeededSound(
//                minecraft.player, entity.getX(), entity.getY(), entity.getZ(), ModSounds.DEATH_GORE.get(),
//                SoundSource.NEUTRAL, 8.0f, 1.0f, minecraft.level.random.nextLong());


        for (int i = 0; i < data.getNumGibs(); i++) {
            var random = entity.getRandom();
            var delta = entity.getDeltaMovement();
            var vx = (0.5 - random.nextDouble()) * 0.35;
            var vz = (0.5 - random.nextDouble()) * 0.35;
            var vy = entity.onGround() ?
                    (random.nextDouble()) * 0.35 :
                    (0.5 - random.nextDouble()) * 0.35;

            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data, vec,
                    new Vec3(delta.x * 0.35 + vx,
                            delta.y * 0.35 + vy,
                            delta.z * 0.35 + vz
                    ),
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            addClientEntity(flyingGibs);
        }
    }

    private static void createDisintegrationFx(LivingEntity entity, Vec3 vec, GoreData data) {
        data.texture = RES_LASER_EFFECT;

        for (int i = 0; i < data.getNumGibs(); i++) {
            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data,
                    vec, Vec3.ZERO,
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            addClientEntity(flyingGibs);
        }
    }

    private static void createBurnFx(LivingEntity entity, Vec3 vec, GoreData data) {
        data.texture = RES_BURN_EFFECT;

        for (int i = 0; i < data.getNumGibs(); i++) {
            var random = entity.getRandom();
            var delta = entity.getDeltaMovement();
            var ax = 0.01;
            var vx = (0.5 - random.nextDouble()) * ax;
            var vz = (0.5 - random.nextDouble()) * ax;
            var vy = entity.onGround() ?
                    (random.nextDouble()) * ax :
                    (0.5 - random.nextDouble()) * ax;

            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data, vec,
                    new Vec3(delta.x * ax + vx,
                            delta.y * ax + vy,
                            delta.z * ax + vz
                    ),
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            addClientEntity(flyingGibs);
        }
    }
}

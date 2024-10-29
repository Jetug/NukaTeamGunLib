package com.nukateam.ntgl.common.foundation.entity.projectile;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.*;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class DeathEffect {
//    public static HashMap<EntityType<?>, GoreData> goreStats = new HashMap<>();
    public static HashMap<Integer, GoreData> goreStats = new HashMap<>();
    private static GoreData genericGore;

    private static final ResourceLocation RES_BIO_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/bio.png");
    private static final ResourceLocation RES_LASER_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/laserdeath.png");

    static {
        var render = (SkeletonRenderer) ClientProxy.getEntityRenderer(EntityType.SKELETON);
        var model = render.getModel();

//        goreStats.put(EntityType.SKELETON, (new GoreData(new ModelGibsBiped(model), 0, 0, 0)));
        genericGore = (new GoreData(null, 160, 21, 31)).setTexture(new ResourceLocation(Ntgl.MOD_ID, "textures/entity/gore.png"));
        genericGore.setRandomScale(0.5f, 0.8f);
    }


    public static void addGoreData(Entity entity, GoreData data) {
        goreStats.put(entity.getId(), data);
    }

    /**
     * Called from ClientProxy in postInit
     */
    public static void postInit() {
        goreStats.values().forEach(stat -> stat.init());
        genericGore.init();
    }

    public static GoreData getGoreData(LivingEntity entity) {
        var data = DeathEffect.goreStats.get(entity.getId());
        if (data == null) {
            data = new GoreData();
            data.bloodColorR = genericGore.bloodColorR;
            data.bloodColorG = genericGore.bloodColorG;
            data.bloodColorB = genericGore.bloodColorB;
//            data.type_main = genericGore.type_main;
//            data.type_trail = genericGore.type_trail;
            data.sound = genericGore.sound;
            goreStats.put(entity.getId(), data);
        }
        return data;
    }

    public static void createDeathEffect(LivingEntity entity, DamageSource deathtype) {
        double x = entity.getX();
        double y = entity.getY() + (entity.getType().getHeight() / 2.0f);
        double z = entity.getZ();

        var data = DeathEffect.getGoreData(entity);

        if (deathtype.is(ModDamageTypes.EXPLOSIVE)) {
            setupGoreData(entity, data);
            createGoreGibs(entity, x, y, z, data);
        }
        else if (deathtype.is(ModDamageTypes.ENERGY)) {
            setupGoreData(entity, data);
            CreateDisintegratedGibs(entity, x, y, z, data);
        }
    }

    private static void CreateDisintegratedGibs(LivingEntity entity, double x, double y, double z, GoreData data) {
        entity.playSound(ModSounds.DEATH_LASER.get(), 1.0f, 1.0f);
        data.texture = RES_LASER_EFFECT;

        for (int i = 0; i < data.getNumGibs(); i++) {
            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data,
                    new Vec3(x, y, z), Vec3.ZERO,
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            entity.level().addFreshEntity(flyingGibs);
        }
    }

    private static void createGoreGibs(LivingEntity entity, double x, double y, double z, GoreData data) {
        entity.playSound(ModSounds.DEATH_GORE.get(), 1.0f, 1.0f);
        var delta = entity.getDeltaMovement();

        for (int i = 0; i < data.getNumGibs(); i++) {
            var random = entity.level().random;
            var vx = (0.5 - random.nextDouble()) * 0.35;
            var vz = (0.5 - random.nextDouble()) * 0.35;
            var vy = entity.onGround() ?
                    (random.nextDouble()) * 0.35 :
                    (0.5 - random.nextDouble()) * 0.35;

            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data,
                    new Vec3(x, y, z),
                    new Vec3(delta.x * 0.35 + vx,
                            delta.y * 0.35 + vy,
                            delta.z * 0.35 + vz
                    ),
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            entity.level().addFreshEntity(flyingGibs);
        }
    }

    private static void setupGoreData(LivingEntity entity, GoreData data) {
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

    public static class GoreData {
        @Nullable public ModelGibs model = null;
        @Nullable public ResourceLocation texture = null;
        public float particleScale = 1.0f;

        int bloodColorR;
        int bloodColorG;
        int bloodColorB;

        //public boolean showBlood = true;
        String fx_main = "GoreFX_Blood";
        String fx_trail = "GoreTrailFX_Blood";
        public SoundEvent sound = ModSounds.DEATH_GORE.get();

//        public TGParticleSystemType type_main;
//        public TGParticleSystemType type_trail;

        public float minPartScale = 1.0f;
        public float maxPartScale = 1.0f;

        public GoreData() {}

        public GoreData(ModelGibs model, int bloodColorR, int bloodColorG, int bloodColorB) {
            this.model = model;
            //		this.modelScale = modelScale;
            this.bloodColorR = bloodColorR;
            this.bloodColorG = bloodColorG;
            this.bloodColorB = bloodColorB;
        }

        public int getNumGibs() {
            return model != null ? model.getNumGibs() : 0;
        }

        public GoreData setTexture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public GoreData setFXscale(float scale) {
            this.particleScale = scale;
            return this;
        }

        public GoreData setFX(String fx_main, String fx_trail) {
            this.fx_main = fx_main;
            this.fx_trail = fx_trail;
            return this;
        }

        public GoreData setSound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public void init() {
//            type_main = new TGParticleSystemType();
//
//            if (TGFX.FXList.containsKey(fx_main.toLowerCase())) {
//                TGFXType fxtype_main = TGFX.FXList.get(fx_main.toLowerCase());
//                if (fxtype_main instanceof TGParticleSystemType) {
//                    this.type_main = getExtendedType((TGParticleSystemType) fxtype_main);
//                } else {
//                    this.type_main = null;
//                }
//            } else {
//                this.type_main = null;
//            }
//
//            type_trail = new TGParticleSystemType();
//
//            if (TGFX.FXList.containsKey(fx_trail.toLowerCase())) {
//                TGFXType fxtype_trail = TGFX.FXList.get(fx_trail.toLowerCase());
//                if (fxtype_trail instanceof TGParticleSystemType) {
//                    this.type_trail = getExtendedType((TGParticleSystemType) fxtype_trail);
//                } else {
//                    this.type_trail = null;
//                }
//            } else {
//                this.type_trail = null;
//            }
        }

        /**
         * Add a random scale to individual gibs.
         */
        public void setRandomScale(float min, float max) {
            minPartScale = min;
            maxPartScale = max;
        }


//        private TGParticleSystemType getExtendedType(TGParticleSystemType supertype) {
//            var type = new TGParticleSystemType();
//
//            type.extend(supertype);
//
//            if (type.colorEntries.size() >= 1) {
//                type.colorEntries.get(0).r = (float) this.bloodColorR / 255.0f;
//                type.colorEntries.get(0).g = (float) this.bloodColorG / 255.0f;
//                type.colorEntries.get(0).b = (float) this.bloodColorB / 255.0f;
//            }
//
//            type.sizeMin *= particleScale;
//            type.sizeMax *= particleScale;
//            type.sizeRateMin *= particleScale;
//            type.sizeRateMax *= particleScale;
//            type.startSizeRateDampingMin *= particleScale;
//            type.startSizeRateMin *= particleScale;
//            type.startSizeRateMax *= particleScale;
//            for (int i = 0; i < type.volumeData.length; i++) {
//                type.volumeData[i] *= particleScale;
//            }
//            return type;
//        }

    }
}

package com.nukateam.ntgl.common.foundation.entity.projectile;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.*;
import com.nukateam.ntgl.common.foundation.entity.FlyingGibs;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import dev.kosmx.playerAnim.core.util.Vec3d;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.DeathType;

public class DeathEffect {
    public static HashMap<EntityType<?>, GoreData> goreStats = new HashMap<>();
    private static GoreData genericGore;

    static {
        var render = (SkeletonRenderer)ClientProxy.getEntityRenderer(EntityType.SKELETON);
        var model = render.getModel();

        goreStats.put(EntityType.SKELETON, (new GoreData(new ModelGibsBiped(model), 0,0,0)));
        genericGore = (new GoreData(null, 160, 21, 31)).setTexture(new ResourceLocation(Ntgl.MOD_ID, "textures/entity/gore.png"));
        genericGore.setRandomScale(0.5f, 0.8f);
      }

    /**
     * Use this method to put GoreData into the map, call this BEFORE postInit()
     *
     * @param entityClass
     * @param data
     */
    public static void addGoreData(EntityType<? extends LivingEntity> entityClass, GoreData data) {
        goreStats.put(entityClass, data);
    }

    /**
     * Called from ClientProxy in postInit
     */
    public static void postInit() {
        goreStats.values().forEach(stat -> stat.init());
        genericGore.init();
    }

    public static GoreData getGoreData(LivingEntity entityClass) {
        var data = DeathEffect.goreStats.get(entityClass.getType());
        if (data == null) {
            data = new GoreData();
            data.bloodColorR = genericGore.bloodColorR;
            data.bloodColorG = genericGore.bloodColorG;
            data.bloodColorB = genericGore.bloodColorB;
//            data.type_main = genericGore.type_main;
//            data.type_trail = genericGore.type_trail;
            data.sound = genericGore.sound;
            data.numGibs = -1; //TODO
            goreStats.put(entityClass.getType(), data);
        }
        return data;
    }

    public static void createDeathEffect(LivingEntity entity, DeathType deathtype, Vec3 delta) {
        double x = entity.getX();
        double y = entity.getY() + (entity.getType().getHeight() / 2.0f);
        double z = entity.getZ();

         if (deathtype == DeathType.GORE) {
            var data = DeathEffect.getGoreData(entity);
            var render = ClientProxy.getEntityRenderer(entity);

            try {
                if (render instanceof GeoEntityRenderer geoRenderer && entity instanceof GeoAnimatable animatable) {
                    var geoModel = geoRenderer.getGeoModel();
                    var model = geoModel.getBakedModel(geoModel.getModelResource(animatable));
                    data.model = new ModelGibsGeo(model, geoRenderer);
                }
                else if (render instanceof LivingEntityRenderer livingRenderer) {
                    if (data.model == null) {
                        var mainModel = livingRenderer.getModel();

                        if (mainModel instanceof HierarchicalModel<? extends Entity> model) {
                            data.model = new ModelGibsGeneric(model);
                        }
                        else if (mainModel instanceof AgeableListModel<? extends Entity> model) {
                            data.model = new ModelGibsAgeable(model);
                        }
                        else {
                            data.model = genericGore.model;
                            data.texture = genericGore.texture;
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            entity.playSound(ModSounds.DEATH_GORE.get(), 1.0f, 1.0f);

            //Spawn MainFX
//            TGParticleSystem sys = new TGParticleSystem(entity.level(), data.type_main, x, entity.getY(), z, entity.xo, entity.yo, entity.zo);
//            ClientProxy.particleManager.addEffect(sys);

            int count;
            if (data.numGibs >= 0) {
                count = data.numGibs;
            } else {
                if (data.model == null)
                    return;
                count = data.model.getNumGibs();
            }

            for (int i = 0; i < count; i++) {
                var random = entity.level().random;
                var vx = (0.5 - random.nextDouble()) * 0.35;
                double vy;

                if (entity.onGround())
                    vy = (random.nextDouble()) * 0.35;
                else
                    vy = (0.5 - random.nextDouble()) * 0.35;
                var vz = (0.5 - random.nextDouble()) * 0.35;

                var ent = new FlyingGibs(entity.level(), entity, data,
                        new Vec3(x, y, z),
                        new Vec3(delta.x * 0.35 + vx,
                                delta.y * 0.35 + vy,
                                delta.z * 0.35 + vz
                        ),
                        (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

                entity.level().addFreshEntity(ent);
            }
        }
//        else if (deathtype == DeathType.BIO) {
//            ClientProxy.get().createFX("biodeath", entity.level(), x, y, z, (double) xo, (double) yo, (double) zo);
//            ClientProxy.get().playSoundOnPosition(TGSounds.DEATH_BIO, (float) x, (float) y, (float) z, 1.0f, 1.0f, false, TGSoundCategory.DEATHEFFECT);
//        }
//        else if (deathtype == DeathType.LASER) {
//            ClientProxy.get().createFX("laserdeathFire", entity.level(), x, y, z, (double) xo, 0, (double) zo);
//            ClientProxy.get().createFX("laserdeathAsh", entity.level(), x, y, z, (double) xo, 0, (double) zo);
//            ClientProxy.get().playSoundOnPosition(TGSounds.DEATH_LASER, (float) x, (float) y, (float) z, 1.0f, 1.0f, false, TGSoundCategory.DEATHEFFECT);
//        }
    }

    public static class GoreData {
        public ModelGibs model = null;
        public ResourceLocation texture = null;
        int numGibs = -1;
        public float particleScale = 1.0f;
        public float modelScale = 1.0f;

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

        public GoreData() {
        }

        public GoreData(ModelGibs model, int bloodColorR, int bloodColorG, int bloodColorB) {
            this.model = model;
            //		this.modelScale = modelScale;
            this.bloodColorR = bloodColorR;
            this.bloodColorG = bloodColorG;
            this.bloodColorB = bloodColorB;
        }

        public GoreData setNumGibs(int gibs) {
            this.numGibs = gibs;
            return this;
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

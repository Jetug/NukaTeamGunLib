package com.nukateam.ntgl.common.foundation.entity.projectile;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.*;
import com.nukateam.ntgl.common.foundation.entity.FlyingGibs;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.DeathType;

public class DeathEffect {

    public static HashMap<EntityType<?>, GoreData> goreStats = new HashMap<>();

    private static GoreData genericGore;

    static {
//        ModelGibs modelBiped = new ModelGibsBiped(new HumanoidModel(0.0f, 0.0f, 64, 64));
//        ModelGibs modelBipedPlayer = new ModelGibsBiped(new HumanoidModel(0.0f, 0.0f, 64, 32));
//        goreStats.put(EntityType.PLAYER, (new GoreData(modelBipedPlayer, 160, 21, 31)));
//        goreStats.put(EntityType.ZOMBIE, (new GoreData(modelBiped, 110, 21, 41)));
//

        var render = (SkeletonRenderer)ClientProxy.getEntityRenderer(EntityType.SKELETON);

        var model = render.getModel();

        goreStats.put(EntityType.SKELETON, (new GoreData(new ModelGibsBiped(model), 0,0,0)));

//        goreStats.put(Villager.class, (new GoreData(new ModelGibsVillager(new ModelVillager(0.0f)), 150, 21, 51)));
//        goreStats.put(Cow.class, (new GoreData(new ModelGibsQuadruped(new ModelCow()), 170, 26, 37)));
//        goreStats.put(Sheep.class, (new GoreData(new ModelGibsQuadruped(new ModelSheep1()), 170, 26, 37)).setFXscale(0.8f));
//        goreStats.put(Chicken.class, (new GoreData(new ModelGibsGeneric(new ModelChicken()), 170, 26, 37)).setFXscale(0.5f));
//        goreStats.put(Creeper.class, (new GoreData(new ModelGibsGeneric(new ModelCreeper()), 50, 175, 57)));
//        goreStats.put(EnderMan.class, (new GoreData(new ModelGibsBiped(new ModelEnderman(0.0f)), 160, 36, 167)));
//        goreStats.put(Pig.class, (new GoreData(new ModelGibsQuadruped(new ModelPig()), 170, 26, 37)).setFXscale(0.8f));
//        goreStats.put(Spider.class, (new GoreData(new ModelGibsGeneric(new ModelSpider()), 85, 156, 17)));
//        goreStats.put(CaveSpider.class, (new GoreData(new ModelGibsGeneric(new ModelSpider()), 85, 156, 17)).setFXscale(0.7f));
////
//        goreStats.put(EntityPigZombie.class, (new GoreData(modelBiped, 110, 51, 11)));
//        goreStats.put(ZombiePigmanSoldier.class, (new GoreData(modelBiped, 110, 51, 11)));
//        goreStats.put(CyberDemon.class, (new GoreData(new ModelGibsBiped(new ModelCyberDemon()), 85, 156, 17)));
//
//        goreStats.put(SuperMutantBasic.class, (new GoreData(new ModelGibsBiped(new ModelSuperMutant()), 109, 60, 25)).setFXscale(1.2f));
//        goreStats.put(SuperMutantElite.class, (new GoreData(new ModelGibsBiped(new ModelSuperMutant()), 109, 60, 25)).setFXscale(1.2f));
//        goreStats.put(SuperMutantHeavy.class, (new GoreData(new ModelGibsBiped(new ModelSuperMutant()), 109, 60, 25)).setFXscale(1.2f));
//
//        goreStats.put(StormTrooper.class, (new GoreData(modelBiped, 160, 21, 31)));
//        goreStats.put(Commando.class, (new GoreData(modelBiped, 160, 21, 31)));
//        goreStats.put(DictatorDave.class, (new GoreData(modelBiped, 160, 21, 31)));
//        goreStats.put(PsychoSteve.class, (new GoreData(modelBiped, 160, 21, 31)));
//
//        goreStats.put(EntityWitch.class, (new GoreData(new ModelGibsVillager(new ModelWitch(1.0f)), 160, 21, 31)));
//        goreStats.put(EntitySlime.class, (new GoreData(new ModelGibsSlime(), 40, 255, 40)));
////
//        goreStats.put(ZombieFarmer.class, (new GoreData(modelBiped, 110, 21, 41)));
//        goreStats.put(ZombieMiner.class, (new GoreData(modelBiped, 110, 21, 41)));
////
//        goreStats.put(Bandit.class, new GoreData(modelBiped, 160, 21, 31));
//        goreStats.put(SkeletonSoldier.class, (new GoreData(new ModelGibsBiped(new ModelSkeleton()), 255, 255, 255)));
//        goreStats.put(AlienBug.class, (new GoreData(new ModelGibsGeneric(new ModelAlienBug()), 235, 255, 70)));
//        goreStats.put(Ghastling.class, (new GoreData(new ModelGibsSlime(), 255, 255, 255)).setFXscale(1.0f));
//
//        goreStats.put(EntityLlama.class, (new GoreData(new ModelGibsQuadruped(new ModelLlama(0f)), 170, 26, 37)));
//        goreStats.put(EntityEvoker.class, (new GoreData(new ModelGibsIllager(new ModelIllager(0.0F, 0.0F, 64, 64)), 110, 21, 41)));
//        goreStats.put(EntityHusk.class, (new GoreData(modelBiped, 110, 21, 41)));
//        goreStats.put(EntityPolarBear.class, (new GoreData(new ModelGibsQuadruped(new ModelPolarBear()), 170, 26, 37)).setFXscale(0.8f));
//        goreStats.put(EntityMagmaCube.class, (new GoreData(new ModelGibsSlime(), 92, 26, 0)));
//        goreStats.put(EntityParrot.class, (new GoreData(new ModelGibsGeneric(new ModelParrot()), 170, 26, 37)).setFXscale(0.5f));
//        goreStats.put(EntityRabbit.class, (new GoreData(new ModelGibsGeneric(new ModelRabbit()), 170, 26, 37)).setFXscale(0.3f));
//        goreStats.put(EntityStray.class, (new GoreData(new ModelGibsBiped(new ModelSkeleton()), 255, 255, 255)));
//        goreStats.put(EntitySilverfish.class, (new GoreData(new ModelGibsGeneric(new ModelSilverfish()), 90, 16, 27)).setFXscale(0.4f));
//        goreStats.put(EntityVindicator.class, (new GoreData(new ModelGibsIllager(new ModelIllager(0.0F, 0.0F, 64, 64)), 110, 21, 41)));
//        goreStats.put(EntityVex.class, (new GoreData(new ModelGibsSlime(), 215, 215, 215)).setFXscale(0.4f));
//        goreStats.put(EntityShulker.class, (new GoreData(new ModelGibsSlime(), 125, 0, 106)).setFXscale(1.5f));
//        goreStats.put(EntityWitherSkeleton.class, (new GoreData(new ModelGibsBiped(new ModelSkeleton()), 50, 50, 50).setFXscale(1.4f)));
//        goreStats.put(EntityGhast.class, (new GoreData(new ModelGibsSlime(), 255, 255, 255)).setFXscale(3.5f));
//        goreStats.put(EntityZombieVillager.class, (new GoreData(modelBiped, 110, 21, 41)));
//        goreStats.put(EntityHorse.class, (new GoreData(new ModelGibsHorse(), 170, 26, 37)).setFXscale(1.0f));
//        goreStats.put(EntityDonkey.class, (new GoreData(new ModelGibsHorse(), 170, 26, 37)).setFXscale(1.0f));
//        goreStats.put(EntityMule.class, (new GoreData(new ModelGibsHorse(), 170, 26, 37)).setFXscale(1.0f));

        genericGore = (new GoreData(null, 160, 21, 31)).setTexture(new ResourceLocation(Ntgl.MOD_ID, "textures/entity/gore.png"));
        genericGore.setRandomScale(0.5f, 0.8f);
        //ModelHorse horse = new ModelHorse();
        //goreStats.put(EntityHorse.class, new GoreData(new ModelGibsHorse(horse), horse.boxList.size(), new ResourceLocation("textures/entity/horse/horse_brown.png"), 0.66f, 150,21,51));
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

    public static void createDeathEffect(LivingEntity entity, DeathType deathtype, float xo, float yo, float zo) {
        //GetEntityType
        //EntityDT entityDT = EntityDeathUtils.getEntityDeathType(entity);

        double x = entity.getX();
        double y = entity.getY() + (entity.getType().getHeight() / 2.0f);
        double z = entity.getZ();

        if (deathtype == DeathType.GORE) {
            var data = DeathEffect.getGoreData(entity);
            var render = ClientProxy.getLivingEntityRenderer(entity);

            try {
                if (data.model == null && render != null) {
                    var mainModel = render.getModel();//(Model) DeathEffectEntityRenderer.RLB_mainModel.get((LivingEntityRenderer) render);

                    if (mainModel instanceof HumanoidModel <? extends Entity> model) {
                        data.model = new ModelGibsBiped(model);
                    }
                    else if (mainModel instanceof QuadrupedModel<? extends LivingEntity> model) {
                        data.model = new ModelGibsQuadruped(model);
                    }
                    else if (mainModel instanceof VillagerModel<? extends LivingEntity> model) {
                        data.model = new ModelGibsVillager(model);
                    }
                    else if(mainModel instanceof HierarchicalModel<? extends Entity> model){
                        data.model = new ModelGibsGeneric(model);
                    }
                    else {
                        data.model = genericGore.model;
                        data.texture = genericGore.texture;
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
//            ClientProxy.get().playSoundOnPosition(data.sound, (float) x, (float) y, (float) z, 1.0f, 1.0f, false, TGSoundCategory.DEATHEFFECT);

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

                var ent = new FlyingGibs(entity.level(), entity, data, x, y, z,
                        xo * 0.35 + vx,
                        yo * 0.35 + vy,
                        zo * 0.35 + vz,
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

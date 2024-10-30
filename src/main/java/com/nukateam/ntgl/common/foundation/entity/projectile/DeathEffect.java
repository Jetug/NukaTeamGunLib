package com.nukateam.ntgl.common.foundation.entity.projectile;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.model.gibs.ModelGibs;
import com.nukateam.ntgl.client.model.gibs.ModelGibsAgeable;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeneric;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeo;
import com.nukateam.ntgl.common.base.utils.DeathType;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class DeathEffect {
    //    public static HashMap<EntityType<?>, GoreData> goreStats = new HashMap<>();
    public static HashMap<Integer, GoreData> goreStats = new HashMap<>();
    private static GoreData genericGore;

    private static final ResourceLocation RES_BIO_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/bio.png");
    private static final ResourceLocation RES_LASER_EFFECT = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/laserdeath.png");

    static {
        genericGore = (new GoreData(null, 160, 21, 31))
                .setTexture(new ResourceLocation(Ntgl.MOD_ID, "textures/entity/gore.png"));
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
            goreStats.put(entity.getId(), data);
        }
        return data;
    }

    public static void createDeathEffect(LivingEntity entity, DamageSource deathtype) {
        double x = entity.getX();
        double y = entity.getY() + (entity.getType().getHeight() / 2.0f);
        double z = entity.getZ();

        var vec = new Vec3(x, y, z);
        var data = DeathEffect.getGoreData(entity);

        if (deathtype.is(ModDamageTypes.EXPLOSIVE)) {
            setupGoreData(entity, data);
            data.gravity = 0.2f;
            data.deathType = DeathType.GORE;
            createGoreGibs(entity, vec, data);
        } else if (deathtype.is(ModDamageTypes.ENERGY)) {
            setupGoreData(entity, data);
            data.gravity = -0.005f;
            data.showBlood = false;
            data.deathType = DeathType.LASER;
            createDisintegratedGibs(entity, vec, data);
        }
    }

    private static void createDisintegratedGibs(LivingEntity entity, Vec3 vec, GoreData data) {
        entity.playSound(ModSounds.DEATH_LASER.get(), 1.0f, 1.0f);
        data.texture = RES_LASER_EFFECT;

        for (int i = 0; i < data.getNumGibs(); i++) {
            var flyingGibs = new FlyingGib(
                    entity.level(), entity, data,
                    vec, Vec3.ZERO,
                    (entity.getType().getWidth() + entity.getType().getHeight()) / 2.0f, i);

            entity.level().addFreshEntity(flyingGibs);
        }
    }

    private static void createGoreGibs(LivingEntity entity, Vec3 vec, GoreData data) {
        entity.playSound(ModSounds.DEATH_GORE.get(), 1.0f, 1.0f);

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

    public static class GoreData implements INBTSerializable<CompoundTag> {
        @Nullable
        public ModelGibs model = null;
        @Nullable
        public ResourceLocation texture = null;
        public float particleScale = 1.0f;
        public float gravity;
        public int bloodColorR;
        public int bloodColorG;
        public int bloodColorB;
        public boolean showBlood = true;
        public SoundEvent sound = ModSounds.DEATH_GORE.get();
        public DeathType deathType = DeathType.DEFAULT;
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

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            if (texture != null)
                tag.putString("texture", texture.toString());
            tag.putFloat("gravity", gravity);

            tag.putInt("bloodColorR", bloodColorR);
            tag.putInt("bloodColorG", bloodColorG);
            tag.putInt("bloodColorB", bloodColorB);
            tag.putBoolean("showBlood", showBlood);
            tag.putInt("deathType", deathType.getValue());

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("texture"))
                texture = new ResourceLocation(tag.getString("texture"));
            if (tag.contains("gravity"))
                gravity = tag.getFloat("gravity");
            if (tag.contains("bloodColorR"))
                bloodColorR = tag.getInt("bloodColorR");
            if (tag.contains("bloodColorG"))
                bloodColorG = tag.getInt("bloodColorG");
            if (tag.contains("bloodColorB"))
                bloodColorB = tag.getInt("bloodColorB");
            if (tag.contains("showBlood"))
                showBlood = tag.getBoolean("showBlood");
            if (tag.contains("deathType"))
                deathType = DeathType.getById(tag.getInt("deathType"));
        }

        public int getNumGibs() {
            return model != null ? model.getNumGibs() : 0;
        }

        public GoreData setTexture(ResourceLocation texture) {
            this.texture = texture;
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
    }
}

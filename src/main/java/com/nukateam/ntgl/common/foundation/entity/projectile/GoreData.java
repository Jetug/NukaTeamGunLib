package com.nukateam.ntgl.common.foundation.entity.projectile;

import com.nukateam.ntgl.client.model.gibs.ModelGibs;
import com.nukateam.ntgl.common.base.utils.DeathType;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class GoreData implements INBTSerializable<CompoundTag> {
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

    /**
     * Add a random scale to individual gibs.
     */
    public void setRandomScale(float min, float max) {
        minPartScale = min;
        maxPartScale = max;
    }
}

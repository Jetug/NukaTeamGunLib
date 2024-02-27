package com.nukateam.gunscore.client.audio;

import com.nukateam.gunscore.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class GunShotSound extends AbstractSoundInstance {
    public GunShotSound(ResourceLocation soundIn, SoundSource categoryIn, double x, double y, double z,
                        float volume, float pitch, boolean reload) {
        super(soundIn, categoryIn);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.attenuation = Attenuation.NONE;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            float distance = reload ? Config.SERVER.reloadMaxDistance.get().floatValue() : Config.SERVER.gunShotMaxDistance.get().floatValue();
            this.volume = volume * (1.0F - Math.min(1.0F, (float) Math.sqrt(player.distanceToSqr(x, y, z)) / distance));
            this.volume *= this.volume; //Ease the volume instead of linear
        }
    }

    public GunShotSound(ResourceLocation soundIn, SoundSource categoryIn, Vec3 position, float volume, float pitch, boolean reload) {
        this(soundIn, categoryIn,
                position.x,
                position.y,
                position.z,
                volume,
                pitch,
                reload
        );
    }
}

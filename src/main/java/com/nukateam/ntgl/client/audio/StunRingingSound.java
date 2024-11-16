package com.nukateam.ntgl.client.audio;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.foundation.init.ModEffects;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

public class StunRingingSound extends AbstractTickableSoundInstance {
    public StunRingingSound() {
        super(ModSounds.ENTITY_STUN_GRENADE_RING.get(), SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.attenuation = Attenuation.NONE;
        this.tick();
    }

    @Override
    public void tick() {
        var player = Minecraft.getInstance().player;
        if (player != null && player.isAlive()) {
            var effect = player.getEffect(ModEffects.DEAFENED.get());
            if (effect != null) {
                this.x = (float) player.getX();
                this.y = (float) player.getY();
                this.z = (float) player.getZ();
                var percent = Math.min((effect.getDuration() / (float) Config.SERVER.soundFadeThreshold.get()), 1);
                this.volume = (float) (percent * Config.SERVER.ringVolume.get());
                return;
            }
        }

        //Stops playing the sound
        this.stop();
    }
}
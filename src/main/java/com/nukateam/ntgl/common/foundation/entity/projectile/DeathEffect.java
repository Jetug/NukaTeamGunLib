package com.nukateam.ntgl.common.foundation.entity.projectile;

import com.nukateam.ntgl.common.data.enums.DeathType;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageEntityDeathFx;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class DeathEffect {
    public static HashMap<Integer, GoreData> goreStats = new HashMap<>();

    public static void addGoreData(Entity entity, GoreData data) {
        goreStats.put(entity.getId(), data);
    }

    public static GoreData getGoreData(LivingEntity entity) {
        var data = DeathEffect.goreStats.get(entity.getId());
        if (data == null) {
            data = new GoreData();
            goreStats.put(entity.getId(), data);
        }
        return data;
    }

    public static void createDeathEffect(LivingEntity entity, DamageSource deathType) {
        var data = DeathEffect.getGoreData(entity);

        if (deathType.is(ModDamageTypes.EXPLOSIVE)) {
            entity.playSound(ModSounds.DEATH_GORE.get(), 8.0f, 1.0f);
            data.deathType = DeathType.GORE;
        }
        else if (deathType.is(ModDamageTypes.ENERGY)) {
            createAshPile(entity);
            entity.playSound(ModSounds.DEATH_LASER.get(), 1.0f, 1.0f);
            data.deathType = DeathType.LASER;
        }
        else if(deathType.is(ModDamageTypes.FIRE)){
            createAshPile(entity);
            entity.playSound(ModSounds.DEATH_LASER.get(), 1.0f, 1.0f);
            data.deathType = DeathType.FIRE;
        }

        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> entity, new S2CMessageEntityDeathFx(entity.getId(), data));
    }

    private static void createAshPile(LivingEntity entity) {
        if(!entity.isInWater()) {
            var pos = new Vec3(entity.getX(), entity.getBlockY(), entity.getZ());
            var ashPile = new AshPile(entity.level(), pos);
            entity.level().addFreshEntity(ashPile);
        }
    }

}

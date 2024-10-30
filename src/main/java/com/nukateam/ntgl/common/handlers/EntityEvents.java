package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();
        ClientProxy.setDamageType(entity, event.getSource());

        if (!entity.level().isClientSide){
            DeathEffect.createDeathEffect(entity, event.getSource());
        }
    }
}

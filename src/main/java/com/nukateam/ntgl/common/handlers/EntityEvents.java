package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.DeathType;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();

        if (!entity.level().isClientSide && event.getSource().is(ModDamageTypes.EXPLOSIVE)){
            DeathEffect.createDeathEffect(entity, DeathType.GORE, entity.getDeltaMovement());
        }
    }
}

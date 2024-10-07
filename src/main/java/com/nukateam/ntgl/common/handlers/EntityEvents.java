package com.nukateam.ntgl.common.handlers;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.utils.EntityDeathUtils;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.helpers.PlayerAnimationHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageEntityData;
import com.nukateam.ntgl.common.network.message.S2CMessageEntityDeath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.ntgl.common.base.utils.EntityDeathUtils.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide) {
//            if (entity instanceof Player) {
//                TGExtendedPlayer tgplayer = TGExtendedPlayer.get((EntityPlayer) event.getEntityLiving());
//                tgplayer.foodleft = 0;
//                tgplayer.lastSaturation = 0;
//                tgplayer.addRadiation(-TGRadiationSystem.RADLOST_ON_DEATH);
//            }


            DeathEffect.createDeathEffect(entity, DeathType.GORE, (float)entity.xo, (float)entity.yo, (float)entity.zo);


//            PacketHandler.getPlayChannel().sendToNearbyPlayers(
//                    () -> LevelLocation.create(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 256),
//                    new S2CMessageEntityDeath(entity, DeathType.GORE));


//            if (event.getSource() instanceof TGDamageSource) {
//                TGDamageSource tgs = (TGDamageSource) event.getSource();
//                if (tgs.deathType != DeathType.DEFAULT) {
//                    if (Math.random() < tgs.goreChance) {
//                        if (EntityDeathUtils.hasSpecialDeathAnim(entity, tgs.deathType)) {
//                            //System.out.println("Send packet!");
//                            TGPackets.network.sendToAllAround(new S2CMessageEntityDeath(entity, tgs.deathType), TGPackets.targetPointAroundEnt(entity, 100.0f));
//                        }
//                    }
//                }
//            }
        }
    }
}

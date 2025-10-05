package com.nukateam.chassis_core.common.events;

import com.nukateam.chassis_core.Global;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void onTick(TickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        switch (event.type) {
            case LEVEL:
                break;
            case PLAYER:
                break;
            case CLIENT:
                Global.CLIENT_TIMER.tick();
                break;
            case SERVER:
                break;
            case RENDER:
                break;
        }
    }

//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        var world = event.getWorld();
//        var pos = event.getPos();
//        var player = event.getPlayer();
//        var state = world.getBlockState(pos);
//        var blockEntity = world.getBlockEntity(pos);
//
//        if (blockEntity instanceof ArmorStationBlockEntity stationBlockEntity) {
//            stationBlockEntity.openGui(player);
//        }
//    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
//        if(event.getEntity() instanceof Player player && isLocalWearingChassis(player)){
//            var damage = ((WearableChassis)player.getVehicle()).getPlayerDamageValue(event.getSource(), event.getAmount());
//            player.hurt(event.getSource(), damage);
//            event.setCanceled(true);
//        }
    }
}
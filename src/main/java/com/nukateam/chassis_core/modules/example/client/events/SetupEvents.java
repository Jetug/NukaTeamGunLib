package com.nukateam.chassis_core.modules.example.client.events;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.client.ExampleChassisRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.chassis_core.modules.example.common.registery.EntityTypes.EXAMPLE_CHASSIS;

@Mod.EventBusSubscriber(modid = ChassisCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EXAMPLE_CHASSIS.get(), ExampleChassisRenderer::new);
    }

}

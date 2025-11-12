package com.nukateam.chassis_core.modules.example.client.events;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.client.ExampleChassisRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.nukateam.chassis_core.modules.example.common.registery.EntityTypes.EXAMPLE_CHASSIS;

@EventBusSubscriber(modid = ChassisCore.MOD_ID, value = Dist.CLIENT)
public class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EXAMPLE_CHASSIS.get(), ExampleChassisRenderer::new);
    }

}

package com.nukateam.chassis_core.client.events;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.client.ClientConfig;
import com.nukateam.chassis_core.client.events.InputEvents;
import com.nukateam.chassis_core.client.input.DoubleClickController;
import com.nukateam.chassis_core.client.input.LongClickController;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@EventBusSubscriber(modid = ChassisCore.MOD_ID@EventBusSubscriber, value = Dist.CLIENT)
public final class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    public static final DoubleClickController DOUBLE_CLICK_CONTROLLER = new DoubleClickController();
    @OnlyIn(Dist.CLIENT)
    private static final LongClickController LONG_CLICK_CONTROLLER = new LongClickController();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientConfig.modResourceManager.loadConfigs();
        registerClickListeners();
    }

    private static void registerClickListeners() {
        DOUBLE_CLICK_CONTROLLER.addListener(com.nukateam.chassis_core.client.events.InputEvents::onDoubleClick);
        LONG_CLICK_CONTROLLER.setRepeatListener(com.nukateam.chassis_core.client.events.InputEvents::onLongClick);
        LONG_CLICK_CONTROLLER.setReleaseListener(InputEvents::onLongRelease);
    }
}
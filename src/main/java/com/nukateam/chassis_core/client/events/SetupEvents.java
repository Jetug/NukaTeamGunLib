package com.nukateam.chassis_core.client.events;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.client.input.DoubleClickController;
import com.nukateam.chassis_core.client.input.LongClickController;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = ChassisCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    public static final DoubleClickController DOUBLE_CLICK_CONTROLLER = new DoubleClickController();
    @OnlyIn(Dist.CLIENT)
    private static final LongClickController LONG_CLICK_CONTROLLER = new LongClickController();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        registerClickListeners();
    }

    private static void registerClickListeners() {
        DOUBLE_CLICK_CONTROLLER.addListener(com.nukateam.chassis_core.client.events.InputEvents::onDoubleClick);
        LONG_CLICK_CONTROLLER.setRepeatListener(com.nukateam.chassis_core.client.events.InputEvents::onLongClick);
        LONG_CLICK_CONTROLLER.setReleaseListener(InputEvents::onLongRelease);
    }
}
package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.helpers.PlayerAnimationHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerAnimatorEvents{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (Ntgl.playerAnimatorLoaded)
            PlayerAnimationHelper.register();
    }
}

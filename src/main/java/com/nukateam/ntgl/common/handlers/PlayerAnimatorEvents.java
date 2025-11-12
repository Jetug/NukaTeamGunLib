package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.helpers.compatibility.PlayerAnimationHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class PlayerAnimatorEvents{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (Ntgl.playerAnimatorLoaded)
            PlayerAnimationHelper.register();
    }
}

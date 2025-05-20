package com.nukateam.ntgl.modules.packs;

import com.nukateam.ntgl.Ntgl;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GunPackModule.MOD_ID)
public class CommonModHandler {
    @SubscribeEvent
    public static void loadGunConfigs(final AddReloadListenerEvent event) {
        event.addListener(new GunConfigLoader());
    }
}


package com.nukateam.example.common;

import com.nukateam.example.common.entities.Brahmin;
import com.nukateam.example.common.entities.Raider;
import com.nukateam.example.common.registery.EntityTypes;
import com.nukateam.ntgl.Ntgl;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityTypes.RAIDER.get(), Raider.createAttributes().build());
        event.put(EntityTypes.BRAHMIN.get(), Brahmin.createAttributes().build());
    }
}
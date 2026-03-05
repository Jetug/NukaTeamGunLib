
package com.nukateam.example.common;

import com.nukateam.example.common.entities.ExampleGeoEntity;
import com.nukateam.example.common.entities.Raider;
import com.nukateam.example.common.registery.EntityTypes;
import com.nukateam.ntgl.Ntgl;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityTypes.RAIDER.get(), Raider.createAttributes().build());
        event.put(EntityTypes.BRAHMIN.get(), ExampleGeoEntity.createAttributes().build());
    }
}
package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class NtglGameEvents {
    public static final ResourceKey<GameEvent> GUNSHOT_EVENT =
            ResourceKey.create(Registries.GAME_EVENT, ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "gunshot_event"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NtglGameEvents::onRegisterGameEvents);
    }

    private static void onRegisterGameEvents(RegisterEvent event) {
        event.register(Registries.GAME_EVENT, helper -> {
            helper.register(GUNSHOT_EVENT, new GameEvent("gunshot_event", 32));
        });
    }

    public static void gunshotEvent(Level level, LivingEntity entity) {
        level.registryAccess().registry(Registries.GAME_EVENT).ifPresent(registry -> {
            var gunshotEvent = registry.get(GUNSHOT_EVENT);
            if (gunshotEvent != null) {
                level.gameEvent(entity, gunshotEvent, entity.blockPosition());
            }
        });
    }
}

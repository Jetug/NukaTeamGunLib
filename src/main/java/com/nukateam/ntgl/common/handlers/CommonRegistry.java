package com.nukateam.ntgl.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.nio.file.Path;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonRegistry {
    private static boolean LOAD_COMPLETE = false;

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        LOAD_COMPLETE = true;
    }

    public static boolean isLoadComplete() {
        return LOAD_COMPLETE;
    }

//    @SubscribeEvent
//    public void onAddPackFinders(AddPackFindersEvent event) {
//        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
//            Path ntglDir = Minecraft.getInstance().gameDirectory.toPath().resolve("ntgl");
//            event.addRepositorySource(new NtglPackFinder(ntglDir));
//        }
//    }
}

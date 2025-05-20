package com.nukateam.ntgl.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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

package com.nukateam.ntgl.modules.packs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class GeneratedGunItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GunPackModule.MOD_ID);
    public static final Map<ResourceLocation, Item> GUN_ITEMS = new HashMap<>();

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
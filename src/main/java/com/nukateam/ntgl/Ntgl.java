package com.nukateam.ntgl;

import com.mojang.logging.LogUtils;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.example.common.registery.EntityTypes;
import com.nukateam.example.common.registery.*;
import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.handler.CrosshairHandler;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.registry.AmmoHolders;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.util.managers.BoundingBoxManager;
import com.nukateam.ntgl.common.datagen.*;
import com.nukateam.ntgl.common.registry.ProjectileRegistry;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.modules.gunpack.GunPackModule;
import com.tiviacz.travelersbackpack.datagen.ModLootTableProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

import java.util.*;

@Mod(Ntgl.MOD_ID)
public class Ntgl {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "ntgl";

    public static boolean controllableLoaded = false;
    public static boolean backpackedLoaded = false;
    public static boolean sophisticatedLoaded = false;
    public static boolean travelersLoaded = false;
    public static boolean yyzBackpackLoaded = false;
    public static boolean curiosLoaded = false;
    public static boolean playerReviveLoaded = false;
    public static boolean playerAnimatorLoaded = false;
    public static boolean subtleEffectsLoaded = false;

    public Ntgl(IEventBus MOD_EVENT_BUS, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        container.registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        container.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        //ModBlocks.REGISTER.register(bus);
        ModContainers.REGISTER.register(MOD_EVENT_BUS);
        ModEffects.REGISTER.register(MOD_EVENT_BUS);
        Projectiles.REGISTER.register(MOD_EVENT_BUS);
        if(Ntgl.isDebugging()) {
            ModItemTabs.register(MOD_EVENT_BUS);
        }

        ExampleWeapons.register(MOD_EVENT_BUS);
        ModParticleTypes.REGISTER.register(MOD_EVENT_BUS);
        ModSounds.REGISTER.register(MOD_EVENT_BUS);
        NtglComponents.REGISTER.register(MOD_EVENT_BUS);
        ModEntityTypes.register(MOD_EVENT_BUS);
        EntityTypes.register(MOD_EVENT_BUS);
        MOD_EVENT_BUS.addListener(this::onCommonSetup);
        MOD_EVENT_BUS.addListener(this::onClientSetup);
        MOD_EVENT_BUS.addListener(this::onGatherData);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MOD_EVENT_BUS.addListener(NtglKeyBinds::registerKeyMappings);
            MOD_EVENT_BUS.addListener(CrosshairHandler::onConfigReload);
        }

        GunPackModule.init(MOD_EVENT_BUS);
        NtglGameEvents.register(MOD_EVENT_BUS);
        new ChassisCore(MOD_EVENT_BUS);
//        TravelersBackpack
        curiosLoaded = ModList.get().isLoaded("curios");
        controllableLoaded = ModList.get().isLoaded("controllable");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        sophisticatedLoaded = ModList.get().isLoaded("sophisticatedbackpacks");
        travelersLoaded = ModList.get().isLoaded("travelersbackpack");
        yyzBackpackLoaded = ModList.get().isLoaded("yyzsbackpack");

        playerReviveLoaded = ModList.get().isLoaded("playerrevive");
        playerAnimatorLoaded = ModList.get().isLoaded("playeranimator");
        subtleEffectsLoaded = ModList.get().isLoaded("subtle_effects");

        AmmoHolders.register();
        AnimationType.register();
//        NeoForge.EVENT_BUS.register(this);
    }

    public static boolean isDebugging() {
        return !FMLEnvironment.production;
    }

    public static NtglOptions getOptions() {
        return NtglOptions.getInstance();
    }

    public static ResourceLocation ntglResource(String name) {
        return ResourceLocation.tryBuild(MOD_ID, name);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModSyncedDataKeys.register();
            ProjectileRegistry.registerProjectiles();

            if (Config.COMMON.gameplay.improvedHitboxes.get()) {
                NeoForge.EVENT_BUS.register(new BoundingBoxManager());
            }
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
    }

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        BlockTagGen blockTagGen = new BlockTagGen(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), LootTableGen.create(output, lookupProvider));
        generator.addProvider(event.includeServer(), blockTagGen);
        generator.addProvider(event.includeServer(), new ItemTagGen(output, lookupProvider, blockTagGen.contentsGetter(), existingFileHelper));
//        generator.addProvider(event.includeServer(), new LanguageGen(generator));
//        generator.addProvider(event.includeServer(), new GunGen(generator));
//        generator.addProvider(event.includeServer(), new DamageTypeGen(output, lookupProvider, existingFileHelper));

//        var damageTypeGenerator = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, NtglDamageTypes::bootstrap);
//        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), damageTypeGenerator, Set.of(Ntgl.MOD_ID)));
        generator.addProvider(event.includeServer(), new DamageTypeGen(output, lookupProvider, existingFileHelper));
    }
}

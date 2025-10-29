package com.nukateam.ntgl;

import com.mojang.logging.LogUtils;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.example.common.registery.EntityTypes;
import com.nukateam.example.common.registery.*;
import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.MetaLoader;
import com.nukateam.ntgl.client.util.handler.CrosshairHandler;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.registry.AmmoHolders;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.util.managers.BoundingBoxManager;
import com.nukateam.ntgl.common.datagen.*;
import com.nukateam.ntgl.common.registry.ProjectileRegistry;
import com.nukateam.ntgl.modules.enchantment.EnchantmentModule;
import com.nukateam.ntgl.common.foundation.crafting.ModRecipeType;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchIngredient;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.modules.gunpack.GunPackModule;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.*;

@Mod(Ntgl.MOD_ID)
public class Ntgl {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "ntgl";
    public static final IEventBus MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

    public static boolean controllableLoaded = false;
    public static boolean backpackedLoaded = false;
    public static boolean travelersLoaded = false;
    public static boolean sophisticatedLoaded = false;
    public static boolean curiosLoaded = false;
    public static boolean playerReviveLoaded = false;
    public static boolean playerAnimatorLoaded = false;
    public static boolean subtleEffectsLoaded = false;

    public Ntgl() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        //ModBlocks.REGISTER.register(bus);
        ModContainers.REGISTER.register(MOD_EVENT_BUS);
        ModEffects.REGISTER.register(MOD_EVENT_BUS);
        Projectiles.REGISTER.register(MOD_EVENT_BUS);
        if(Ntgl.isDebugging()) {
            ModItemTabs.register(MOD_EVENT_BUS);
        }

        ModGuns.register(MOD_EVENT_BUS);
        ModRecipeType.REGISTER.register(MOD_EVENT_BUS);
        ModParticleTypes.REGISTER.register(MOD_EVENT_BUS);
        ModRecipeSerializers.REGISTER.register(MOD_EVENT_BUS);
        ModSounds.REGISTER.register(MOD_EVENT_BUS);
        ModTileEntities.REGISTER.register(MOD_EVENT_BUS);
        ModEntityTypes.register(MOD_EVENT_BUS);
        EntityTypes.register(MOD_EVENT_BUS);
        MOD_EVENT_BUS.addListener(this::onCommonSetup);
        MOD_EVENT_BUS.addListener(this::onClientSetup);
        MOD_EVENT_BUS.addListener(this::onGatherData);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
            MOD_EVENT_BUS.addListener(NtglKeyBinds::registerKeyMappings);
            MOD_EVENT_BUS.addListener(CrosshairHandler::onConfigReload);
            MOD_EVENT_BUS.addListener(ClientHandler::onRegisterReloadListener);
        });

        GunPackModule.init(MOD_EVENT_BUS);
        EnchantmentModule.init(MOD_EVENT_BUS);
        NtglGameEvents.register(MOD_EVENT_BUS);
        new ChassisCore(MOD_EVENT_BUS);
//        TravelersBackpack
        curiosLoaded = ModList.get().isLoaded("curios");
        controllableLoaded = ModList.get().isLoaded("controllable");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        travelersLoaded = ModList.get().isLoaded("travelersbackpack");
        sophisticatedLoaded = ModList.get().isLoaded("sophisticatedbackpacks");
        playerReviveLoaded = ModList.get().isLoaded("playerrevive");
        playerAnimatorLoaded = ModList.get().isLoaded("playeranimator");
        subtleEffectsLoaded = ModList.get().isLoaded("subtle_effects");

        AmmoHolders.register();
        AnimationType.register();
        MinecraftForge.EVENT_BUS.register(this);
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
            PacketHandler.init();
            ModSyncedDataKeys.register();
            CraftingHelper.register(new ResourceLocation(MOD_ID, "workbench_ingredient"),
                    WorkbenchIngredient.Serializer.INSTANCE);

            ProjectileRegistry.registerProjectiles();

            if (Config.COMMON.gameplay.improvedHitboxes.get()) {
                MinecraftForge.EVENT_BUS.register(new BoundingBoxManager());
            }
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
    }

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, NtglDamageTypes::bootstrap);

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        BlockTagGen blockTagGen = new BlockTagGen(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), new RecipeGen(output));
        generator.addProvider(event.includeServer(), new LootTableGen(output));
        generator.addProvider(event.includeServer(), blockTagGen);
        generator.addProvider(event.includeServer(), new ItemTagGen(output, lookupProvider, blockTagGen.contentsGetter(), existingFileHelper));
//        generator.addProvider(event.includeServer(), new LanguageGen(generator));
//        generator.addProvider(event.includeServer(), new GunGen(generator));
//        generator.addProvider(event.includeServer(), new DamageTypeGen(output, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), BUILDER, Set.of(Ntgl.MOD_ID)));
    }
}

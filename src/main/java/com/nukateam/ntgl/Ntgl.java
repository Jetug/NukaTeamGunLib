package com.nukateam.ntgl;

import com.mojang.logging.LogUtils;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.nukateam.example.common.registery.EntityTypes;
import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.example.common.registery.ModItemTabs;
import com.nukateam.ntgl.client.ClientHandler;
import com.nukateam.ntgl.client.MetaLoader;
import com.nukateam.ntgl.client.data.handler.CrosshairHandler;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.common.base.utils.BoundingBoxManager;
import com.nukateam.ntgl.common.base.utils.ProjectileManager;
import com.nukateam.ntgl.common.data.datagen.BlockTagGen;
import com.nukateam.ntgl.common.data.datagen.DamageTypeGen;
import com.nukateam.ntgl.common.data.datagen.ItemTagGen;
import com.nukateam.ntgl.common.data.datagen.LootTableGen;
import com.nukateam.ntgl.common.foundation.ModBlocks;
import com.nukateam.ntgl.common.foundation.crafting.ModRecipeType;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchIngredient;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.network.PacketHandler;
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

import java.util.Set;

import static com.nukateam.example.common.registery.ModGuns.*;

@Mod(Ntgl.MOD_ID)
public class Ntgl {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "ntgl";
    public static final IEventBus MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

    public static boolean controllableLoaded = false;
    public static boolean backpackedLoaded = false;
    public static boolean playerReviveLoaded = false;
    public static boolean playerAnimatorLoaded = false;

    public Ntgl() {
//        AzureLib.initialize();

        ModGuns.register(MOD_EVENT_BUS);
        ModBlocks.register(MOD_EVENT_BUS);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        //ModBlocks.REGISTER.register(bus);
        ModContainers.REGISTER.register(MOD_EVENT_BUS);
        ModEffects.REGISTER.register(MOD_EVENT_BUS);
        ModEnchantments.REGISTER.register(MOD_EVENT_BUS);
        Projectiles.REGISTER.register(MOD_EVENT_BUS);
        ModItemTabs.register(MOD_EVENT_BUS);
        ModRecipeType.REGISTER.register(MOD_EVENT_BUS);
        ModParticleTypes.REGISTER.register(MOD_EVENT_BUS);
        ModRecipeSerializers.REGISTER.register(MOD_EVENT_BUS);
        ModSounds.REGISTER.register(MOD_EVENT_BUS);
        ModTileEntities.REGISTER.register(MOD_EVENT_BUS);
        EntityTypes.register(MOD_EVENT_BUS);
        MOD_EVENT_BUS.addListener(this::onCommonSetup);
        MOD_EVENT_BUS.addListener(this::onClientSetup);
        MOD_EVENT_BUS.addListener(this::onGatherData);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
            MOD_EVENT_BUS.addListener(KeyBinds::registerKeyMappings);
            MOD_EVENT_BUS.addListener(CrosshairHandler::onConfigReload);
            MOD_EVENT_BUS.addListener(ClientHandler::onRegisterReloadListener);
        });

        controllableLoaded = ModList.get().isLoaded("controllable");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        playerReviveLoaded = ModList.get().isLoaded("playerrevive");
        playerAnimatorLoaded = ModList.get().isLoaded("playeranimator");

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static boolean isDebugging() {
        return !FMLEnvironment.production;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.init();
            ModSyncedDataKeys.register();
            CraftingHelper.register(new ResourceLocation(MOD_ID, "workbench_ingredient"),
                    WorkbenchIngredient.Serializer.INSTANCE);

            registerProjectiles();

            if (Config.COMMON.gameplay.improvedHitboxes.get()) {
                MinecraftForge.EVENT_BUS.register(new BoundingBoxManager());
            }
        });
    }

    private static void registerProjectiles() {
        ProjectileManager.getInstance().registerFactory(GRENADE.get(),
                (worldIn, entity, weapon, item, modifiedGun) -> new GrenadeEntity(Projectiles.GRENADE.get(), worldIn, entity, weapon, item, modifiedGun));

//        ProjectileManager.getInstance().registerFactory(MISSILE.get(),
//                (worldIn, entity, weapon, item, modifiedGun) -> new MissileEntity(Projectiles.MISSILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND10MM.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                        new LaserProjectile(Projectiles.LASER_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND45.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                        new TeslaProjectile(Projectiles.TESLA_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND38.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                        new ContinuousLaserProjectile(Projectiles.CONTINUOUS_LASER_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND5MM.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                        new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
    }

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        BlockTagGen blockTagGen = new BlockTagGen(output, lookupProvider, existingFileHelper);
//        generator.addProvider(event.includeServer(), new RecipeGen(generator));
        generator.addProvider(event.includeServer(), new LootTableGen(output));
        generator.addProvider(event.includeServer(), blockTagGen);
        generator.addProvider(event.includeServer(), new ItemTagGen(output, lookupProvider, blockTagGen.contentsGetter(), existingFileHelper));
//        generator.addProvider(event.includeServer(), new LanguageGen(generator));
//        generator.addProvider(event.includeServer(), new GunGen(generator));
        generator.addProvider(event.includeServer(), new DamageTypeGen(output, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                output, event.getLookupProvider(), BUILDER, Set.of(Ntgl.MOD_ID)));
    }
}

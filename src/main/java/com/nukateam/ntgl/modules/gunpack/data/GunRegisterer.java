package com.nukateam.ntgl.modules.gunpack.data;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.AttachmentItem;
import com.nukateam.ntgl.common.foundation.item.attachment.GenericAttachmentItem;
import com.nukateam.ntgl.common.util.helpers.RegistrationHelper;
import com.nukateam.ntgl.modules.gunpack.GunPackModule;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix.GSON;
import static net.minecraft.world.item.CreativeModeTab.builder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunRegisterer {
    private static final String REGISTRY_FILE = "registry.json";
    private static final Pattern CONFIG_PATTERN = Pattern.compile("^data/([^/]+)/guns/([^/]+\\.json)$");
    private static final Pattern RECIPE_PATTERN = Pattern.compile("^data/([^/]+)/recipes/([^/]+\\.json)$");
    private static final Map<String, DeferredRegister<Item>> ITEMS = new HashMap<>();
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GunPackModule.MOD_ID);
    private static final Map<String, Set<String>> MOD_CONFIGS = new HashMap<>();
    private static boolean hasValidRecipe = false;

    public static void init(IEventBus eventBus) {
        processArchives();
//        registerWeapons();
        registerGunTab();

        ITEMS.forEach((k, gunRegister)-> {
            gunRegister.register(eventBus);
        });
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private static void registerGunTab() {
        if(hasValidRecipe){
            createBlockTab(CREATIVE_MODE_TABS);
        }
        ITEMS.forEach((id, gunRegister)-> {
            createTab(CREATIVE_MODE_TABS, id, gunRegister);
        });
    }

    @Nullable
    private static void createBlockTab(DeferredRegister<CreativeModeTab> creativeModeTabs) {
        creativeModeTabs.register("ntgl_blocks",
                () -> builder().icon(() -> new ItemStack(ModBlocks.WORKBENCH.get()))
                        .title(Component.translatable("itemGroup." + GunPackModule.MOD_ID + ".blocks"))
                        .displayItems((params, output) -> {
                            output.accept(ModBlocks.WORKBENCH.get());
                        }).build());

    }

    private static void createTab(DeferredRegister<CreativeModeTab> creativeModeTabs, String namespace, DeferredRegister<Item> gunRegister) {
        var items = gunRegister.getEntries();
        if(!items.isEmpty()) {
            creativeModeTabs.register(namespace,
                    () -> builder().icon(() -> new ItemStack(items.stream().findFirst().get().get()))
                            .title(Component.translatable("itemGroup." + namespace))
                            .displayItems((params, output) -> {
                                for (var entry : gunRegister.getEntries()) {
                                    RegistrationHelper.registerGunOrDefault(output, entry.get());
                                }
                            }).build());
        }
    }

    public static void processArchives() {
        Path ntglPath = Paths.get("ntgl");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(ntglPath, "*.zip")) {
            for (Path zipFile : stream) {
                processPack(zipFile);
                processZipArchive(zipFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processZipArchive(Path zipPath) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            var entries = zip.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) continue;

//                checkConfig(entry);

                if(!hasValidRecipe && isValidRecipe(entry, zip)) {
                    hasValidRecipe = true;
                }

            }
        } catch (IOException e) {
            GunPackModule.LOGGER.error("Error processing archive: {}", zipPath, e);
        }
    }

    private static void processPack(Path packPath) {
        try (var zip = new ZipFile(packPath.toFile())) {
            var manifest = zip.getEntry(REGISTRY_FILE);
            if (manifest == null) return;

            var manifestJson = GSON.fromJson(
                    new InputStreamReader(zip.getInputStream(manifest)),
                    JsonObject.class
            );

            var guns = manifestJson.getAsJsonArray("guns");
            guns.forEach(item -> {
                var id = ResourceLocation.tryParse(item.getAsString());
                if (id != null && !ForgeRegistries.ITEMS.containsKey(id)) {
                    registerGun(id.getNamespace(), id.getPath());
                }
            });

            var attachments = manifestJson.getAsJsonArray("attachments");
            attachments.forEach(item -> {
                var id = ResourceLocation.tryParse(item.getAsString());
                if (id != null && !ForgeRegistries.ITEMS.containsKey(id)) {
                    registerAttachment(id.getNamespace(), id.getPath());
                }
            });

        } catch (Exception e) {
            GunPackModule.LOGGER.error("Error processing pack: {}", packPath.getFileName(), e);
            e.printStackTrace();
        }
    }

    private static void checkConfig(ZipEntry entry) {
        var matcher = CONFIG_PATTERN.matcher(entry.getName());
        if (matcher.matches()) {
            String modId = matcher.group(1);
            String configName = matcher.group(2);
            MOD_CONFIGS.computeIfAbsent(modId, k -> new HashSet<>()).add(configName);
        }
    }

    private static void registerWeapons() {
        MOD_CONFIGS.forEach((modId, configs) -> {
            configs.forEach(configName -> {
                var weaponName = configName.replace(".json", "");
                var weaponId = ResourceLocation.tryBuild(modId, weaponName);

                if(!ForgeRegistries.ITEMS.containsKey(weaponId)) {
                    registerGun(modId, weaponName);
                }
            });
        });
    }

    private static boolean isValidRecipe(ZipEntry entry, ZipFile zip) {
        try {
            var pathMatcher = RECIPE_PATTERN.matcher(entry.getName());
            if(!pathMatcher.matches()) return false;

            try (var reader = new BufferedReader(
                    new InputStreamReader(zip.getInputStream(entry)))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\"type\": \"ntgl:workbench\"")) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading recipe: " + entry.getName());
        }
        return false;
    }

    private static void registerGun(String namespace, String name) {
        var gunRegister = ITEMS.computeIfAbsent(namespace, id -> DeferredRegister.create(ForgeRegistries.ITEMS, id));
        gunRegister.register(name, () -> new GunItem(
                new Item.Properties().stacksTo(1)
        ));
    }

    private static void registerAttachment(String namespace, String name) {
        var register = ITEMS.computeIfAbsent(namespace, id -> DeferredRegister.create(ForgeRegistries.ITEMS, id));
        register.register(name, () -> new GenericAttachmentItem(
                new Item.Properties().stacksTo(1)
        ));
    }
}
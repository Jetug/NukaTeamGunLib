package com.nukateam.ntgl.modules.gunpack.data;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.helpers.RegistrationHelper;
import com.nukateam.ntgl.modules.gunpack.GunPackModule;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.minecraft.world.item.CreativeModeTab.builder;

public class GunRegisterer {
    private static final Pattern CONFIG_PATTERN = Pattern.compile("^data/([^/]+)/guns/([^/]+\\.json)$");
    private static final Pattern RECIPE_PATTERN = Pattern.compile("^data/([^/]+)/recipes/([^/]+\\.json)$");
    private static final Map<String, DeferredRegister<Item>> ITEMS = new HashMap<>();
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GunPackModule.MOD_ID);
    private static final Map<String, Set<String>> MOD_CONFIGS = new HashMap<>();
    private static boolean hasValidRecipe = false;
    private static RegistryObject<CreativeModeTab> GUN_TAB = null;

    public static void init(IEventBus eventBus) {
        processArchives();
        registerWeapons();
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
//            var gunTab = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, id);
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
                processZipArchive(zipFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processZipArchive(Path zipPath) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                checkConfig(entry);
                if(!hasValidRecipe && isValidRecipe(entry, zip)) {
                    hasValidRecipe = true;
                }

            }
        } catch (IOException e) {
            System.err.println("Error processing archive: " + zipPath);
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

    private static void registerWeapons() {
        MOD_CONFIGS.forEach((modId, configs) -> {
            configs.forEach(configName -> {
                var weaponId = configName.replace(".json", "");
                var gunRegister = ITEMS.computeIfAbsent(modId, id -> DeferredRegister.create(ForgeRegistries.ITEMS, id));
                gunRegister.register(weaponId, () -> new GunItem(
                        new Item.Properties().stacksTo(1)
                ));
            });
        });
    }
}
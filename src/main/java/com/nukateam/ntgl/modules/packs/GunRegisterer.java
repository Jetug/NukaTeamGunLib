package com.nukateam.ntgl.modules.packs;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.nukateam.example.common.registery.ModGuns.ITEMS;

public class GunRegisterer {
    private static final Pattern CONFIG_PATTERN = Pattern.compile("^data/([^/]+)/guns/([^/]+\\.json)$");
    private static final Map<String, DeferredRegister<Item>> ITEMS = new HashMap<>();
    private static final Map<String, Set<String>> MOD_CONFIGS = new HashMap<>();

    public static void init(IEventBus eventBus) {
        processArchives();
        registerWeapons();

        ITEMS.forEach((k, gunRegister)-> {
            gunRegister.register(eventBus);
        });
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
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                Matcher matcher = CONFIG_PATTERN.matcher(entry.getName());
                if (matcher.matches()) {
                    String modId = matcher.group(1);
                    String configName = matcher.group(2);
                    MOD_CONFIGS.computeIfAbsent(modId, k -> new HashSet<>()).add(configName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing archive: " + zipPath);
            e.printStackTrace();
        }
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
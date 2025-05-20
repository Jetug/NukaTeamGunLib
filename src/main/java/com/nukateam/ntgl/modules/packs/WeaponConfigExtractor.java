package com.nukateam.ntgl.modules.packs;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WeaponConfigExtractor {
    public static void findGuns() {
        var ntglPath = Paths.get("ntgl");
        var configPattern = Pattern.compile("^data/([^/]+)/guns/([^/]+\\.json)$");
        var modConfigs = new HashMap<String, Set<String>>();

        try (var stream = Files.newDirectoryStream(ntglPath, "*.zip")) {
            for (Path zipFile : stream) {
                processZipFile(zipFile, configPattern, modConfigs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printResults(modConfigs);
    }

    private static void processZipFile(Path zipPath, Pattern pattern, Map<String, Set<String>> result) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                var entryName = entry.getName();
                var matcher = pattern.matcher(entryName);

                // Теперь будут находиться только файлы с .json
                if (matcher.matches()) {  // Используем matches() вместо find() для полного совпадения
                    String modId = matcher.group(1);
                    String configName = matcher.group(2);
                    result.computeIfAbsent(modId, k -> new HashSet<>()).add(configName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing zip file: " + zipPath);
            e.printStackTrace();
        }
    }

    private static void printResults(Map<String, Set<String>> configMap) {
        configMap.forEach((modId, configs) -> {
            configs.forEach(config -> {
                var rl = ResourceLocation.tryBuild(modId, config);
                createGunItem(rl);
            });
        });
    }

    private static void createGunItem(ResourceLocation id) {
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            var properties  = new Item.Properties().stacksTo(1);
            var gunItem     = new GunItem(properties);
//            GeneratedGunItems.GUN_ITEMS.put(id, gunConfig);
            GeneratedGunItems.ITEMS.register(id.getPath(), () -> gunItem);
        }
    }
}
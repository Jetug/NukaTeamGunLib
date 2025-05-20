package com.nukateam.ntgl.modules.packs;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NTGLPackManager {
    private static final List<Path> RESOURCE_PACKS = new ArrayList<>();
    private static final List<Path> DATA_PACKS = new ArrayList<>();

    public static void scanPacks() {
        var packsDir = FMLPaths.GAMEDIR.get().resolve("ntgl");

        try {
            if (!Files.exists(packsDir)) {
                Files.createDirectories(packsDir);
            }

            RESOURCE_PACKS.clear();
            DATA_PACKS.clear();

            Files.list(packsDir)
                    .filter(p -> p.toString().endsWith(".zip"))
                    .forEach(NTGLPackManager::processZip);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processZip(Path zipPath) {
        try (FileSystem fs = FileSystems.newFileSystem(zipPath, (ClassLoader) null)) {
            boolean hasAssets = Files.exists(fs.getPath("assets"));
            boolean hasData = Files.exists(fs.getPath("data"));

            if (hasAssets) RESOURCE_PACKS.add(zipPath);
            if (hasData) DATA_PACKS.add(zipPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void addResourcePacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            addPacks(event, RESOURCE_PACKS, "assets");
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            addPacks(event, DATA_PACKS, "data");
        }
    }

    private static void addPacks(AddPackFindersEvent event, List<Path> packs, String type) {
        for (Path packPath : packs) {
            Pack.ResourcesSupplier supplier = (packId) -> {
                try {
                    return new NTGLPackResources(packPath, type, packId);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create pack resources for " + packPath, e);
                }
            };

            var packInfo = Pack.readPackInfo(
                    "ntgl/" + packPath.getFileName(),
                    supplier
            );

            if (packInfo != null) {
                var pack = Pack.create(
                        "ntgl/" + packPath.getFileName(),  // ID
                        Component.literal("NTGL Pack: " + packPath.getFileName()),  // Title
                        true,  // Required
                        supplier,  // ResourcesSupplier
                        packInfo,  // Info
                        event.getPackType(),  // PackType
                        Pack.Position.TOP,  // Position
                        false,  // FixedPosition
                        PackSource.BUILT_IN  // PackSource
                );

                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        }
    }
}
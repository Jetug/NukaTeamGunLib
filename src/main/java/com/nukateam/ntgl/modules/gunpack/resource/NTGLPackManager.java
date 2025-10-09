package com.nukateam.ntgl.modules.gunpack.resource;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
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

            Files.list(packsDir).forEach(NTGLPackManager::processPack);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processPack(Path packPath) {
        try {
            var isZip = Files.isRegularFile(packPath) && packPath.toString().endsWith(".zip");
            var isFolder = Files.isDirectory(packPath);

            if (!isZip && !isFolder) return;

            var fs = isZip ?
                    FileSystems.newFileSystem(packPath, (ClassLoader) null) :
                    FileSystems.getDefault();

            var root = isZip ? fs.getPath("/") : packPath;

            var hasAssets = Files.exists(root.resolve("assets"));
            var hasData = Files.exists(root.resolve("data"));

            if (hasAssets) RESOURCE_PACKS.add(packPath);
            if (hasData) DATA_PACKS.add(packPath);

            if (isZip) fs.close();

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
            PackLocationInfo locInfo = new PackLocationInfo(
                    "ntgl/" + packPath.getFileName(),
                    Component.literal("NTGL Pack: " + packPath.getFileName()),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );


            Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) {
                    try {
                        return new NTGLPackResources(packPath, type, info.id());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create pack resources for " + packPath, e);
                    }
                }
                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata meta) {
                    return openPrimary(info);
                }
            };

            Pack pack = Pack.readMetaAndCreate(
                    locInfo,
                    supplier,
                    event.getPackType(),
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );

            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        }
    }
}

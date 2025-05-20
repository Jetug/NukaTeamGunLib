package com.nukateam.ntgl.modules.packs;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.ResourcePackLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.packs.repository.Pack;

import javax.annotation.Nullable;

public class NTGLPackResources implements PackResources {
    private final FileSystem fs;
    private final String type;
    private final String packId;

    public NTGLPackResources(Path path, String type) throws IOException {
        this.fs = FileSystems.newFileSystem(path, (ClassLoader) null);
        this.type = type;
        this.packId = "ntgl_" + path.getFileName().toString().replace(".zip", "");
    }

    public NTGLPackResources(Path path, String type, String packId) throws IOException {
        this.fs = FileSystems.newFileSystem(path, (ClassLoader) null);
        this.type = type;
        this.packId = packId;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        Path path = fs.getPath(String.join("/", paths));
        if (Files.exists(path)) {
            return () -> Files.newInputStream(path);
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        Path path = fs.getPath(type, location.getNamespace(), location.getPath());
        if (Files.exists(path)) {
            return () -> Files.newInputStream(path);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
        Path root = fs.getPath(type, namespace, path);
        if (Files.exists(root)) {
            try (Stream<Path> walk = Files.walk(root, Integer.MAX_VALUE)) {
                walk.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String relative = root.relativize(file).toString().replace('\\', '/');
                            ResourceLocation loc = new ResourceLocation(namespace, path + "/" + relative);
                            output.accept(loc, () -> Files.newInputStream(file));
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        try {
            Path root = fs.getPath(type);
            if (Files.exists(root)) {
                return Files.list(root)
                        .filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        if (serializer.getMetadataSectionName().equals("pack")) {
            IoSupplier<InputStream> supplier = getRootResource("pack.mcmeta");
            if (supplier != null) {
                try (InputStream stream = supplier.get()) {
                    return AbstractPackResources.getMetadataFromStream(serializer, stream);
                }
            }
        }
        return null;
    }

    @Override
    public String packId() {
        return packId;
    }

    @Override
    public void close() {
        try {
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
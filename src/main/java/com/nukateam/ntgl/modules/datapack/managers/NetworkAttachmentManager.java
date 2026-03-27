package com.nukateam.ntgl.modules.datapack.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nukateam.ntgl.modules.constants.Paths;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.modules.datapack.DataUtils;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class NetworkAttachmentManager extends SimplePreparableReloadListener<Map<IAttachment<?>, AttachmentConfig>> {
    private static List<IAttachment<?>> clientRegisteredAttachments = new ArrayList<>();
    private static NetworkAttachmentManager instance;

    private Map<ResourceLocation, AttachmentConfig> registeredAttachments = new HashMap<>();

    public static void onServerStopped() {
        NetworkAttachmentManager.instance = null;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkAttachmentManager networkManager = new NetworkAttachmentManager();
        event.addListener(networkManager);
        NetworkAttachmentManager.instance = networkManager;
    }

    @Override
    protected Map<IAttachment<?>, AttachmentConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return DataUtils.getConfigMap(manager, (v) -> v instanceof IAttachment<?>, AttachmentConfig.class, Paths.ATTACHMENTS);
    }

    @Override
    protected void apply(Map<IAttachment<?>, AttachmentConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, AttachmentConfig> builder = ImmutableMap.builder();

        objects.forEach((abstractItem, config) -> {
            if(abstractItem instanceof Item item) {
                Validate.notNull(ITEMS.getKey(item));
                builder.put(ITEMS.getKey(item), config);
                abstractItem.setConfig(new ConfigSupplier<>(config));
            }
        });

        this.registeredAttachments = builder.build();
    }

    public void writeRegistered(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.registeredAttachments.size());
        this.registeredAttachments.forEach((id, config) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(config.serializeNBT());
        });
    }

    public static ImmutableMap<ResourceLocation, AttachmentConfig> readRegistered(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            var builder = ImmutableMap.<ResourceLocation, AttachmentConfig>builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                AttachmentConfig config = AttachmentConfig.create(id, buffer.readNbt());
                builder.put(id, config);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static void updateRegisteredAttachments(Map<ResourceLocation, AttachmentConfig> registered) {
        clientRegisteredAttachments.clear();
        if (registered != null) {
            for (Map.Entry<ResourceLocation, AttachmentConfig> entry : registered.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof IAttachment<?>)) {
                    return;
                }
                ((IAttachment<?>) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredAttachments.add((IAttachment<?>) item);
            }
        }
    }

    public Map<ResourceLocation, AttachmentConfig> getRegisteredAttachments() {
        return this.registeredAttachments;
    }

    public static List<IAttachment<?>> getClientRegisteredAttachments() {
        return ImmutableList.copyOf(clientRegisteredAttachments);
    }

    @Nullable
    public static NetworkAttachmentManager get() {
        return instance;
    }

    public static class Supplier {
        private final AttachmentConfig config;

        private Supplier(AttachmentConfig config) {
            this.config = config;
        }

        public AttachmentConfig getConfig() {
            return this.config;
        }
    }
}

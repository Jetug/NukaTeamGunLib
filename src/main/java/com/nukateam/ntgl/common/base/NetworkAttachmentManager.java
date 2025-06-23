package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkAttachmentManager extends SimplePreparableReloadListener<Map<IAttachment<?>, AttachmentConfig>> {
    private static List<IAttachment<?>> clientRegisteredAttachments = new ArrayList<>();
    private static NetworkAttachmentManager instance;

    private Map<ResourceLocation, AttachmentConfig> registeredAttachments = new HashMap<>();

    @Override
    protected Map<IAttachment<?>, AttachmentConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, (v) -> v instanceof IAttachment<?>, AttachmentConfig.class, "attachments");
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

    public static boolean updateRegisteredAttachments(S2CMessageUpdateAttachments message) {
        return updateRegisteredAttachments(message.getRegistered());
    }

    private static boolean updateRegisteredAttachments(Map<ResourceLocation, AttachmentConfig> registered) {
        clientRegisteredAttachments.clear();
        if (registered != null) {
            for (Map.Entry<ResourceLocation, AttachmentConfig> entry : registered.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof IAttachment<?>)) {
                    return false;
                }
                ((IAttachment<?>) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredAttachments.add((IAttachment<?>) item);
            }
            return true;
        }
        return false;
    }

    public Map<ResourceLocation, AttachmentConfig> getRegisteredAttachments() {
        return this.registeredAttachments;
    }

    public static List<IAttachment<?>> getClientRegisteredAttachments() {
        return ImmutableList.copyOf(clientRegisteredAttachments);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkAttachmentManager.instance = null;
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkAttachmentManager networkManager = new NetworkAttachmentManager();
        event.addListener(networkManager);
        NetworkAttachmentManager.instance = networkManager;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAttachments());
        }
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

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkAttachmentManager.get());
            NetworkAttachmentManager.get().writeRegistered(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registered = NetworkAttachmentManager.readRegistered(buffer);
            NetworkAttachmentManager.updateRegisteredAttachments(registered);
            return Optional.empty();
        }
    }
}

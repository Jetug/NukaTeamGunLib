package com.nukateam.ntgl.modules.datapack.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateThrowable;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.modules.datapack.DataUtils;
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

public class NetworkGrenadeManager extends SimplePreparableReloadListener<Map<IThrowable, ThrowableConfig>> {
    public static final String PATH = "throwable";
    private static final List<IThrowable> clientRegisteredAttachments = new ArrayList<>();
    private static NetworkGrenadeManager instance;

    private Map<ResourceLocation, ThrowableConfig> registeredAttachments = new HashMap<>();

    public static void onServerStopped() {
        NetworkGrenadeManager.instance = null;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkGrenadeManager networkManager = new NetworkGrenadeManager();
        event.addListener(networkManager);
        NetworkGrenadeManager.instance = networkManager;
    }

    @Override
    protected Map<IThrowable, ThrowableConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return DataUtils.getConfigMap(manager, (v) -> v instanceof IThrowable, ThrowableConfig.class, PATH);
    }

    @Override
    protected void apply(Map<IThrowable, ThrowableConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, ThrowableConfig> builder = ImmutableMap.builder();

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

    public static ImmutableMap<ResourceLocation, ThrowableConfig> readRegistered(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            var builder = ImmutableMap.<ResourceLocation, ThrowableConfig>builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                ThrowableConfig config = ThrowableConfig.create(id, buffer.readNbt());
                builder.put(id, config);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredConfigs(S2CMessageUpdateThrowable message) {
        return updateRegisteredConfigs(message.getRegistered());
    }

    private static boolean updateRegisteredConfigs(Map<ResourceLocation, ThrowableConfig> registered) {
        clientRegisteredAttachments.clear();
        if (registered != null) {
            for (Map.Entry<ResourceLocation, ThrowableConfig> entry : registered.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof IThrowable)) {
                    return false;
                }
                ((IThrowable) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredAttachments.add((IThrowable) item);
            }
            return true;
        }
        return false;
    }

    public Map<ResourceLocation, ThrowableConfig> getRegisteredAttachments() {
        return this.registeredAttachments;
    }

    public static List<IThrowable> getClientRegisteredAttachments() {
        return ImmutableList.copyOf(clientRegisteredAttachments);
    }

    @Nullable
    public static NetworkGrenadeManager get() {
        return instance;
    }

    public static class Supplier {
        private final ThrowableConfig config;

        private Supplier(ThrowableConfig config) {
            this.config = config;
        }

        public ThrowableConfig getConfig() {
            return this.config;
        }
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkGrenadeManager.get());
            NetworkGrenadeManager.get().writeRegistered(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registered = NetworkGrenadeManager.readRegistered(buffer);
            NetworkGrenadeManager.updateRegisteredConfigs(registered);
            return Optional.empty();
        }
    }
}

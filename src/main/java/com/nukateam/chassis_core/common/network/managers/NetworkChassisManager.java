package com.nukateam.chassis_core.common.network.managers;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.modules.config.utils.ConfigUtils;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.Registries;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = ChassisCore.MOD_ID)
public class NetworkChassisManager extends SimplePreparableReloadListener<Map<EntityType<Chassis>, ChassisConfig>> {
    public static final String PATH = "cc/chassis";
    private static NetworkChassisManager instance;

    private Map<ResourceLocation, ChassisConfig> registeredConfig = new HashMap<>();

    @Nullable
    public static NetworkChassisManager get() {
        return instance;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkChassisManager networkGunManager = new NetworkChassisManager();
        event.addListener(networkGunManager);
        NetworkChassisManager.instance = networkGunManager;
    }

    public static void stop() {
        NetworkChassisManager.instance = null;
    }

    @Override
    protected Map<EntityType<Chassis>, ChassisConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, Registries.ENTITY_TYPE, (v) -> true, ChassisConfig.class, PATH);
    }

    @Override
    protected void apply(Map<EntityType<Chassis>, ChassisConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, ChassisConfig>builder();

        objects.forEach((chassis, config) -> {
            Validate.notNull(Registries.ENTITY_TYPE.getKey((chassis)));
            builder.put(Registries.ENTITY_TYPE.getKey(chassis), config);
            Configs.CHASSIS_CONFIGS.put(chassis, new ConfigSupplier<>(config));
        });

        this.registeredConfig = builder.build();
    }

    public void writeRegisteredConfig(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.registeredConfig.size());
        this.registeredConfig.forEach((id, config) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(config.serializeNBT());
        });
    }

    public static ImmutableMap<ResourceLocation, ChassisConfig> readRegisteredConfigs(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            var builder = ImmutableMap.<ResourceLocation, ChassisConfig>builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var config = ChassisConfig.create(buffer.readNbt());
                builder.put(id, config);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredConfig(Map<ResourceLocation, ChassisConfig> registeredConfig) {
        if (registeredConfig != null) {
            for (Map.Entry<ResourceLocation, ChassisConfig> entry : registeredConfig.entrySet()) {
                var item = Registries.ENTITY_TYPE.getValue(entry.getKey());
                Configs.CHASSIS_CONFIGS.put((EntityType<Chassis>) item, new ConfigSupplier<>(entry.getValue()));
            }
            return true;
        }
        return false;
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkChassisManager.get());
            NetworkChassisManager.get().writeRegisteredConfig(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registeredConfig = NetworkChassisManager.readRegisteredConfigs(buffer);
            NetworkChassisManager.updateRegisteredConfig(registeredConfig);
            return Optional.empty();
        }
    }
}

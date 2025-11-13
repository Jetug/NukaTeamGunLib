package com.nukateam.chassis_core.common.network.managers;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.foundation.item.IChassisEquipment;
import com.nukateam.chassis_core.modules.config.utils.ConfigUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = ChassisCore.MOD_ID)
public class NetworkEquipmentManager extends SimplePreparableReloadListener<Map<IChassisEquipment, EquipmentConfig>> {
    public static final String PATH = "cc/equipment";
    private static NetworkEquipmentManager instance;

    private Map<ResourceLocation, EquipmentConfig> registeredConfig = new HashMap<>();

    private NetworkEquipmentManager(){}

    @SubscribeEvent
    public static void register(AddReloadListenerEvent event) {
        var networkGunManager = new NetworkEquipmentManager();
        event.addListener(networkGunManager);
        NetworkEquipmentManager.instance = networkGunManager;
    }

    @Override
    protected Map<IChassisEquipment, EquipmentConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, BuiltInRegistries.ITEM, (v) -> v instanceof IChassisEquipment, EquipmentConfig.class, PATH);
    }

    @Override
    protected void apply(Map<IChassisEquipment, EquipmentConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, EquipmentConfig>builder();

        objects.forEach((item, config) -> {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey((Item) item);
            Validate.notNull(key);
            builder.put(key, config);
            item.setConfig(new ConfigSupplier<>(config));
            Configs.EQUIPMENT_CONFIGS.put(item, new ConfigSupplier<>(config));
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

    public static ImmutableMap<ResourceLocation, EquipmentConfig> readRegisteredConfigs(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            var builder = ImmutableMap.<ResourceLocation, EquipmentConfig>builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var config = EquipmentConfig.create(buffer.readNbt());
                builder.put(id, config);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredConfig(Map<ResourceLocation, EquipmentConfig> registeredConfig) {
        if (registeredConfig != null) {
            for (Map.Entry<ResourceLocation, EquipmentConfig> entry : registeredConfig.entrySet()) {
                var item = BuiltInRegistries.ITEM.get(entry.getKey());
                if (item instanceof IChassisEquipment chassisEquipment) {
                    Configs.EQUIPMENT_CONFIGS.put(chassisEquipment, new ConfigSupplier<>(entry.getValue()));
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    public static NetworkEquipmentManager get() {
        return instance;
    }

    public static void stop() {
        instance = null;
    }

    public static class LoginData{
//        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkEquipmentManager.get());
            NetworkEquipmentManager.get().writeRegisteredConfig(buffer);
        }

//        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registeredConfig = NetworkEquipmentManager.readRegisteredConfigs(buffer);
            NetworkEquipmentManager.updateRegisteredConfig(registeredConfig);
            return Optional.empty();
        }
    }
}
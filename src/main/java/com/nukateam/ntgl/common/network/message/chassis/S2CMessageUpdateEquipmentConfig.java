package com.nukateam.ntgl.common.network.message.chassis;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.chassis_core.client.network.ClientPlayHandler;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkEquipmentManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateEquipmentConfig  {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateEquipmentConfig> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private ImmutableMap<ResourceLocation, EquipmentConfig> registeredConfigs;

    public S2CMessageUpdateEquipmentConfig() {}

    public static void encode(S2CMessageUpdateEquipmentConfig message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkEquipmentManager.get());
        NetworkEquipmentManager.get().writeRegisteredConfig(buffer);
    }

    public static S2CMessageUpdateEquipmentConfig decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateEquipmentConfig message = new S2CMessageUpdateEquipmentConfig();
        message.registeredConfigs = NetworkEquipmentManager.readRegisteredConfigs(buffer);
        return message;
    }

    public static void handle(S2CMessageUpdateEquipmentConfig message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateEquipment(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, EquipmentConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }
}

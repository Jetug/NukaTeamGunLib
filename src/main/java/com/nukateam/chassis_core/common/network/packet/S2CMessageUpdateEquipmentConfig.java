package com.nukateam.chassis_core.common.network.packet;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.client.network.ClientPlayHandler;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.network.managers.NetworkEquipmentManager;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateEquipmentConfig extends PlayMessage<S2CMessageUpdateEquipmentConfig> {
    private ImmutableMap<ResourceLocation, EquipmentConfig> registeredConfigs;

    public S2CMessageUpdateEquipmentConfig() {}

    @Override
    public void encode(S2CMessageUpdateEquipmentConfig message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkEquipmentManager.get());
        NetworkEquipmentManager.get().writeRegisteredConfig(buffer);
    }

    @Override
    public S2CMessageUpdateEquipmentConfig decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateEquipmentConfig message = new S2CMessageUpdateEquipmentConfig();
        message.registeredConfigs = NetworkEquipmentManager.readRegisteredConfigs(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateEquipmentConfig message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateEquipment(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, EquipmentConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }
}

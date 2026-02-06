package com.nukateam.chassis_core.common.network.packet;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.client.network.ClientPlayHandler;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkEquipmentManager;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateEquipmentConfig implements IMessage<S2CMessageUpdateEquipmentConfig> {
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
    public void handle(S2CMessageUpdateEquipmentConfig message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateEquipment(message)));
        supplier.setPacketHandled(true);
    }

    public ImmutableMap<ResourceLocation, EquipmentConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }
}

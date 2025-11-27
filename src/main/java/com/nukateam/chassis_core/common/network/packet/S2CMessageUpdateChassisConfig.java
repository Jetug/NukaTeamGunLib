package com.nukateam.chassis_core.common.network.packet;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.client.network.*;
import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.chassis_core.common.network.managers.NetworkChassisManager;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateChassisConfig implements IMessage<S2CMessageUpdateChassisConfig> {
    private ImmutableMap<ResourceLocation, ChassisConfig> registeredConfigs;

    public S2CMessageUpdateChassisConfig() {}

    @Override
    public void encode(S2CMessageUpdateChassisConfig message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkChassisManager.get());
        NetworkChassisManager.get().writeRegisteredConfig(buffer);
    }

    @Override
    public S2CMessageUpdateChassisConfig decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateChassisConfig message = new S2CMessageUpdateChassisConfig();
        message.registeredConfigs = NetworkChassisManager.readRegisteredConfigs(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateChassisConfig message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateChassis(message)));
        supplier.setPacketHandled(true);
    }

    public ImmutableMap<ResourceLocation, ChassisConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }
}

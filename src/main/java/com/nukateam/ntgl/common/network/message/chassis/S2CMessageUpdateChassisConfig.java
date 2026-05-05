package com.nukateam.ntgl.common.network.message.chassis;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.client.network.*;
import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkChassisManager;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateChassisConfig  {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateChassisConfig> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private ImmutableMap<ResourceLocation, ChassisConfig> registeredConfigs;

    public S2CMessageUpdateChassisConfig() {}

    public static void encode(S2CMessageUpdateChassisConfig message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkChassisManager.get());
        NetworkChassisManager.get().writeRegisteredConfig(buffer);
    }

    public static S2CMessageUpdateChassisConfig decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateChassisConfig message = new S2CMessageUpdateChassisConfig();
        message.registeredConfigs = NetworkChassisManager.readRegisteredConfigs(buffer);
        return message;
    }

    public static void handle(S2CMessageUpdateChassisConfig message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateChassis(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, ChassisConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }
}

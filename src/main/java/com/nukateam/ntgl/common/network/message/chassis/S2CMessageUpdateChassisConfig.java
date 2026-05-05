package com.nukateam.ntgl.common.network.message.chassis;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.client.network.*;
import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.datapack.managers.NetworkChassisManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateChassisConfig implements CustomPacketPayload{
    public static final Type<S2CMessageUpdateChassisConfig> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_update_chassis_config"));

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

    public static void handle(S2CMessageUpdateChassisConfig message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateChassis(message)));
    }

    public ImmutableMap<ResourceLocation, ChassisConfig> getRegisteredConfig() {
        return this.registeredConfigs;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

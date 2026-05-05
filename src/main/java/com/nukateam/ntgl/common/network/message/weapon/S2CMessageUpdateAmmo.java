package com.nukateam.ntgl.common.network.message.weapon;

import com.google.common.collect.ImmutableMap;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAmmoManager;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateAmmo implements CustomPacketPayload {
    public static final Type<S2CMessageUpdateAmmo> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_update_ammo"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateAmmo> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private ImmutableMap<ResourceLocation, ProjectileConfig> registeredGuns;

    public S2CMessageUpdateAmmo() {}

    public static void encode(S2CMessageUpdateAmmo message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAmmoManager.get());
        NetworkAmmoManager.get().writeRegisteredAmmo(buffer);
    }

    public static S2CMessageUpdateAmmo decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateAmmo message = new S2CMessageUpdateAmmo();
        message.registeredGuns = NetworkAmmoManager.readRegisteredAmmo(buffer);
        return message;
    }

    public static void handle(S2CMessageUpdateAmmo message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateAmmo(message)));
    }

    public ImmutableMap<ResourceLocation, ProjectileConfig> getRegisteredAmmo() {
        return this.registeredGuns;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

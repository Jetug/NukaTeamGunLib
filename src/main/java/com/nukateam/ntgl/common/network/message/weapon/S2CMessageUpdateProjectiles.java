package com.nukateam.ntgl.common.network.message.weapon;

import com.google.common.collect.ImmutableMap;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkProjectileManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateProjectiles implements CustomPacketPayload {
    private ImmutableMap<ResourceLocation, ProjectileConfig> registeredGuns;

    public static final Type<S2CMessageUpdateProjectiles> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_update_projectiles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateProjectiles> CODEC = StreamCodec.of(
            S2CMessageUpdateProjectiles::encode,
            S2CMessageUpdateProjectiles::decode);


    public S2CMessageUpdateProjectiles() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buffer, S2CMessageUpdateProjectiles message) {
        Validate.notNull(NetworkProjectileManager.get());
        NetworkProjectileManager.get().write(buffer);
    }

    public static S2CMessageUpdateProjectiles decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateProjectiles message = new S2CMessageUpdateProjectiles();
        message.registeredGuns = NetworkProjectileManager.read(buffer);
        return message;
    }

    public static void handle(S2CMessageUpdateProjectiles message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateProjectile(message)));
    }

    public ImmutableMap<ResourceLocation, ProjectileConfig> getRegisteredAmmo() {
        return this.registeredGuns;
    }
}

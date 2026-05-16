package com.nukateam.ntgl.common.network.message.weapon;

import com.google.common.collect.ImmutableMap;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkProjectileManager;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateProjectiles implements IMessage<S2CMessageUpdateProjectiles> {
    private ImmutableMap<ResourceLocation, ProjectileConfig> registeredGuns;

    public S2CMessageUpdateProjectiles() {}

    @Override
    public void encode(S2CMessageUpdateProjectiles message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkProjectileManager.get());
        NetworkProjectileManager.get().write(buffer);
    }

    @Override
    public S2CMessageUpdateProjectiles decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateProjectiles message = new S2CMessageUpdateProjectiles();
        message.registeredGuns = NetworkProjectileManager.read(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateProjectiles message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateProjectile(message)));
        supplier.setPacketHandled(true);
    }

    public ImmutableMap<ResourceLocation, ProjectileConfig> getRegisteredAmmo() {
        return this.registeredGuns;
    }
}

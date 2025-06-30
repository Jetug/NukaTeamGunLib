package com.nukateam.ntgl.common.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAmmoManager;
import com.nukateam.ntgl.common.data.config.Projectile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateAmmo extends PlayMessage<S2CMessageUpdateAmmo> {
    private ImmutableMap<ResourceLocation, Projectile> registeredGuns;

    public S2CMessageUpdateAmmo() {}

    @Override
    public void encode(S2CMessageUpdateAmmo message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAmmoManager.get());
        NetworkAmmoManager.get().writeRegisteredAmmo(buffer);
    }

    @Override
    public S2CMessageUpdateAmmo decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateAmmo message = new S2CMessageUpdateAmmo();
        message.registeredGuns = NetworkAmmoManager.readRegisteredAmmo(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAmmo message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateAmmo(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, Projectile> getRegisteredAmmo() {
        return this.registeredGuns;
    }
}

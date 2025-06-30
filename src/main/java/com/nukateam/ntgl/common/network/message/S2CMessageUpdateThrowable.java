package com.nukateam.ntgl.common.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkGrenadeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateThrowable extends PlayMessage<S2CMessageUpdateThrowable> {
    private ImmutableMap<ResourceLocation, ThrowableConfig> registered;

    public S2CMessageUpdateThrowable() {}

    @Override
    public void encode(S2CMessageUpdateThrowable message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkGrenadeManager.get());
        NetworkGrenadeManager.get().writeRegistered(buffer);
    }

    @Override
    public S2CMessageUpdateThrowable decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateThrowable();
        message.registered = NetworkGrenadeManager.readRegistered(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateThrowable message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateThrowable(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, ThrowableConfig> getRegistered() {
        return this.registered;
    }
}

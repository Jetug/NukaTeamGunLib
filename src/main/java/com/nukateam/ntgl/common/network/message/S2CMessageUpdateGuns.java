package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkGunManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateGuns extends PlayMessage<S2CMessageUpdateGuns> {
    private ImmutableMap<ResourceLocation, WeaponConfig> registeredGuns;

    public S2CMessageUpdateGuns() {}

    @Override
    public void encode(S2CMessageUpdateGuns message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkGunManager.get());
        NetworkGunManager.get().writeRegisteredGuns(buffer);
    }

    @Override
    public S2CMessageUpdateGuns decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateGuns();
        message.registeredGuns = NetworkGunManager.readRegisteredGuns(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateGuns message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateGuns(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, WeaponConfig> getRegisteredGuns() {
        return this.registeredGuns;
    }
}

package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.ClientPlayHandler;
import com.nukateam.ntgl.common.base.config.CustomGun;
import com.nukateam.ntgl.common.base.CustomGunLoader;
import com.nukateam.ntgl.common.base.config.Gun;
import com.nukateam.ntgl.common.base.NetworkGunManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateGuns extends PlayMessage<S2CMessageUpdateGuns> {
    private ImmutableMap<ResourceLocation, Gun> registeredGuns;
    private ImmutableMap<ResourceLocation, CustomGun> customGuns;

    public S2CMessageUpdateGuns() {
    }

    @Override
    public void encode(S2CMessageUpdateGuns message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkGunManager.get());
        Validate.notNull(CustomGunLoader.get());
        NetworkGunManager.get().writeRegisteredGuns(buffer);
        CustomGunLoader.get().writeCustomGuns(buffer);
    }

    @Override
    public S2CMessageUpdateGuns decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateGuns message = new S2CMessageUpdateGuns();
        message.registeredGuns = NetworkGunManager.readRegisteredGuns(buffer);
        message.customGuns = CustomGunLoader.readCustomGuns(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateGuns message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateGuns(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, Gun> getRegisteredGuns() {
        return this.registeredGuns;
    }

    public ImmutableMap<ResourceLocation, CustomGun> getCustomGuns() {
        return this.customGuns;
    }
}

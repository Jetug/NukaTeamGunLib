package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.modules.datapack.managers.NetworkWeaponManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateWeapons extends PlayMessage<S2CMessageUpdateWeapons> {
    private ImmutableMap<ResourceLocation, WeaponConfig> registeredGuns;

    public S2CMessageUpdateWeapons() {}

    @Override
    public void encode(S2CMessageUpdateWeapons message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkWeaponManager.get());
        NetworkWeaponManager.get().writeRegisteredGuns(buffer);
    }

    @Override
    public S2CMessageUpdateWeapons decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateWeapons();
        message.registeredGuns = NetworkWeaponManager.readRegisteredWeapons(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateWeapons message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateWeapons(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, WeaponConfig> getRegisteredGuns() {
        return this.registeredGuns;
    }
}

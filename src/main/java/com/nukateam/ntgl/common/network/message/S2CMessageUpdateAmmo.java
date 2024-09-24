package com.nukateam.ntgl.common.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.ClientPlayHandler;
import com.nukateam.ntgl.common.base.CustomAmmoLoader;
import com.nukateam.ntgl.common.base.CustomGunLoader;
import com.nukateam.ntgl.common.base.NetworkAmmoManager;
import com.nukateam.ntgl.common.base.NetworkGunManager;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.CustomAmmo;
import com.nukateam.ntgl.common.base.gun.CustomGun;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateAmmo extends PlayMessage<S2CMessageUpdateAmmo> {
    private ImmutableMap<ResourceLocation, Ammo> registeredGuns;
    private ImmutableMap<ResourceLocation, CustomAmmo> customGuns;

    public S2CMessageUpdateAmmo() {
    }

    @Override
    public void encode(S2CMessageUpdateAmmo message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAmmoManager.get());
        Validate.notNull(CustomGunLoader.get());
        NetworkAmmoManager.get().writeRegisteredAmmo(buffer);
        CustomGunLoader.get().writeCustomGuns(buffer);
    }

    @Override
    public S2CMessageUpdateAmmo decode(FriendlyByteBuf buffer) {
        S2CMessageUpdateAmmo message = new S2CMessageUpdateAmmo();
        message.registeredGuns = NetworkAmmoManager.readRegisteredAmmo(buffer);
        message.customGuns = CustomAmmoLoader.readCustomAmmo(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAmmo message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateAmmo(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, Ammo> getRegisteredAmmo() {
        return this.registeredGuns;
    }

    public ImmutableMap<ResourceLocation, CustomAmmo> getCustomAmmo() {
        return this.customGuns;
    }
}

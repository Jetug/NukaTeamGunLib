package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.CustomAmmoManager;
import com.nukateam.ntgl.client.CustomGunManager;
import com.nukateam.ntgl.common.base.NetworkAmmoManager;
import com.nukateam.ntgl.common.base.NetworkGunManager;
import com.nukateam.ntgl.common.network.message.*;
import com.mrcrayfish.framework.api.FrameworkAPI;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler {
    private static FrameworkNetwork PLAY_CHANNEL;

    public static FrameworkNetwork getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void init() {
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Ntgl.MOD_ID, "play"), 1)
                .registerPlayMessage(C2SMessageAim.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageReload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageShoot.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageUnload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageCraft.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageAttachments.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageShooting.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SRequestEntityData.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessagePreFireSound.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageHandAction.class, MessageDirection.PLAY_SERVER_BOUND)

                .registerPlayMessage(S2CMessageEntityDeath.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageEntityData.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageReload.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageStunGrenade.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBulletTrail.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateGuns.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateAmmo.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBlood.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageGunSound.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitBlock.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitEntity.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageRemoveProjectile.class, MessageDirection.PLAY_CLIENT_BOUND)
                .build();

        FrameworkAPI.registerLoginData(new ResourceLocation(Ntgl.MOD_ID, "network_gun_manager"), NetworkGunManager.LoginData::new);
        FrameworkAPI.registerLoginData(new ResourceLocation(Ntgl.MOD_ID, "custom_gun_manager"), CustomGunManager.LoginData::new);

        FrameworkAPI.registerLoginData(new ResourceLocation(Ntgl.MOD_ID, "network_ammo_manager"), NetworkAmmoManager.LoginData::new);
        FrameworkAPI.registerLoginData(new ResourceLocation(Ntgl.MOD_ID, "custom_ammo_manager"), CustomAmmoManager.LoginData::new);
    }
}

package com.nukateam.gunscore.common.network;

import com.nukateam.gunscore.client.CustomGunManager;
import com.nukateam.gunscore.common.base.NetworkGunManager;
import com.nukateam.gunscore.common.network.message.*;
import com.nukateam.gunscore.GunMod;
import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkChannelBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static SimpleChannel PLAY_CHANNEL;

    public static SimpleChannel getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void init() {
        PLAY_CHANNEL = FrameworkChannelBuilder
            .create(GunMod.MOD_ID, "guns", 1)
                .registerPlayMessage(C2SMessageAim.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(C2SMessageReload.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(C2SMessageCharge.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(MessageShoot.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(C2SMessageUnload.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(C2SMessageCraft.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(C2SMessageAttachments.class, NetworkDirection.PLAY_TO_SERVER)
                .registerPlayMessage(MessageShooting.class, NetworkDirection.PLAY_TO_SERVER)

                .registerPlayMessage(S2CMessageReload.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageStunGrenade.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(MessageBulletTrail.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageUpdateGuns.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageBlood.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(MessageGunSound.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageProjectileHitBlock.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageProjectileHitEntity.class, NetworkDirection.PLAY_TO_CLIENT)
                .registerPlayMessage(S2CMessageRemoveProjectile.class, NetworkDirection.PLAY_TO_CLIENT)
                .build();

        FrameworkAPI.registerLoginData(new ResourceLocation(GunMod.MOD_ID, "network_gun_manager"), NetworkGunManager.LoginData::new);
        FrameworkAPI.registerLoginData(new ResourceLocation(GunMod.MOD_ID, "custom_gun_manager"), CustomGunManager.LoginData::new);
    }
}

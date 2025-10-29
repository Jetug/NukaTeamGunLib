package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.network.*;
import com.nukateam.ntgl.*;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAmmoManager;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAttachmentManager;
import com.nukateam.ntgl.modules.datapack.managers.NetworkWeaponManager;
import com.nukateam.ntgl.common.network.message.*;
import com.mrcrayfish.framework.api.*;
import net.minecraft.resources.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class PacketHandler {
    private static FrameworkNetwork PLAY_CHANNEL;

    public static FrameworkNetwork getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void init() {
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(ResourceLocation.tryBuild(Ntgl.MOD_ID, "play"), 1)
                .registerPlayMessage(C2SMessageAim.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageReload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageShoot.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageUnload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageCraft.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageAttachments.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageShooting.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessagePreFireSound.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageHandAction.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageMeleeAttack.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageGrenade.class, MessageDirection.PLAY_SERVER_BOUND)

                .registerPlayMessage(S2CMessagePlayerAnimation.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageEntityDeath.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageEntityDeathFx.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageReload.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageStunGrenade.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBulletTrail.class, MessageDirection.PLAY_CLIENT_BOUND)

                .registerPlayMessage(S2CMessageUpdateWeapons.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateAmmo.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateAttachments.class, MessageDirection.PLAY_CLIENT_BOUND)

                .registerPlayMessage(S2CMessageBlood.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageGunSound.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitBlock.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitEntity.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitFluid.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageRemoveProjectile.class, MessageDirection.PLAY_CLIENT_BOUND)

                .registerPlayMessage(S2CMessageProjectileExplosion.class, MessageDirection.PLAY_CLIENT_BOUND)
                .build();

        FrameworkAPI.registerLoginData(ResourceLocation.tryBuild(Ntgl.MOD_ID, "network_gun_manager"), NetworkWeaponManager.LoginData::new);
        FrameworkAPI.registerLoginData(ResourceLocation.tryBuild(Ntgl.MOD_ID, "network_ammo_manager"), NetworkAmmoManager.LoginData::new);
        FrameworkAPI.registerLoginData(ResourceLocation.tryBuild(Ntgl.MOD_ID, "network_attachment_manager"), NetworkAttachmentManager.LoginData::new);
    }

    public static void sendAnimation(LivingEntity entity, InteractionHand hand, AnimationType animation) {
        var levelLoc = LevelLocation.create(entity.level(), entity.blockPosition());
        getPlayChannel().sendToNearbyPlayers(() -> levelLoc,
                new S2CMessagePlayerAnimation(entity.getId(), animation, hand));
    }
}

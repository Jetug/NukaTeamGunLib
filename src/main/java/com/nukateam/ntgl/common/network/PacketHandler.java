package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.network.message.chassis.*;
import com.nukateam.ntgl.common.network.message.weapon.*;
import com.nukateam.ntgl.modules.data.message.S2CMessageUpdateEntityData;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.function.Supplier;
//@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class PacketHandler {
//    @SubscribeEvent
//    public static void register(RegisterPayloadHandlersEvent event) {
//        var registrar = event.registrar("1");
//        registrar.playToServer(C2SMessageAim.TYPE, C2SMessageAim.CODEC, C2SMessageAim::handle);
//    }

    private static FrameworkNetwork PLAY_CHANNEL;

    public static FrameworkNetwork getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void init() {
        var id = 0;
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(ResourceLocation.tryBuild(Ntgl.MOD_ID, "ntgl"), 1)
                .registerPlayMessage(String.valueOf(id++), C2SActionPacket.class, C2SActionPacket.STREAM_CODEC, C2SActionPacket::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SGenericPacket.class, C2SGenericPacket.STREAM_CODEC, C2SGenericPacket::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CInventoryPacket.class, S2CInventoryPacket.STREAM_CODEC, S2CInventoryPacket::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateChassisConfig.class, S2CMessageUpdateChassisConfig.STREAM_CODEC, S2CMessageUpdateChassisConfig::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateEquipmentConfig.class, S2CMessageUpdateEquipmentConfig.STREAM_CODEC, S2CMessageUpdateEquipmentConfig::handle, PacketFlow.CLIENTBOUND)

                .registerPlayMessage(String.valueOf(id++), C2SMessageAim.class, C2SMessageAim.STREAM_CODEC, C2SMessageAim::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageReload.class, C2SMessageReload.STREAM_CODEC, C2SMessageReload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageShoot.class, C2SMessageShoot.STREAM_CODEC, C2SMessageShoot::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageUnload.class, C2SMessageUnload.STREAM_CODEC, C2SMessageUnload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageReloadStop.class, C2SMessageReloadStop.STREAM_CODEC, C2SMessageReloadStop::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageCraft.class, C2SMessageCraft.STREAM_CODEC, C2SMessageCraft::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageAttachments.class, C2SMessageAttachments.STREAM_CODEC, C2SMessageAttachments::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageChangeAmmo.class, C2SMessageChangeAmmo.STREAM_CODEC, C2SMessageChangeAmmo::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageShooting.class, C2SMessageShooting.STREAM_CODEC, C2SMessageShooting::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessagePreFireSound.class, C2SMessagePreFireSound.STREAM_CODEC, C2SMessagePreFireSound::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageHandAction.class, C2SMessageHandAction.STREAM_CODEC, C2SMessageHandAction::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageMeleeAttack.class, C2SMessageMeleeAttack.STREAM_CODEC, C2SMessageMeleeAttack::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageGrenade.class, C2SMessageGrenade.STREAM_CODEC, C2SMessageGrenade::handle, PacketFlow.SERVERBOUND)

                .registerPlayMessage(String.valueOf(id++), S2CMessagePlayerAnimation.class, S2CMessagePlayerAnimation.STREAM_CODEC, S2CMessagePlayerAnimation::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageEntityDeath.class, S2CMessageEntityDeath.STREAM_CODEC, S2CMessageEntityDeath::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageEntityDeathFx.class, S2CMessageEntityDeathFx.STREAM_CODEC, S2CMessageEntityDeathFx::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageStunGrenade.class, S2CMessageStunGrenade.STREAM_CODEC, S2CMessageStunGrenade::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateWeapons.class, S2CMessageUpdateWeapons.STREAM_CODEC, S2CMessageUpdateWeapons::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateAmmo.class, S2CMessageUpdateAmmo.STREAM_CODEC, S2CMessageUpdateAmmo::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateAttachments.class, S2CMessageUpdateAttachments.STREAM_CODEC, S2CMessageUpdateAttachments::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageBlood.class, S2CMessageBlood.STREAM_CODEC, S2CMessageBlood::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageGunSound.class, S2CMessageGunSound.STREAM_CODEC, S2CMessageGunSound::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitBlock.class, S2CMessageProjectileHitBlock.STREAM_CODEC, S2CMessageProjectileHitBlock::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitEntity.class, S2CMessageProjectileHitEntity.STREAM_CODEC, S2CMessageProjectileHitEntity::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitFluid.class, S2CMessageProjectileHitFluid.STREAM_CODEC, S2CMessageProjectileHitFluid::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileExplosion.class, S2CMessageProjectileExplosion.STREAM_CODEC, S2CMessageProjectileExplosion::handle, PacketFlow.CLIENTBOUND)

                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateEntityData.class, S2CMessageUpdateEntityData.STREAM_CODEC, S2CMessageUpdateEntityData::handle, PacketFlow.CLIENTBOUND)
                .build();
    }

    public static void sendAnimation(LivingEntity entity, InteractionHand hand, AnimationType animation) {
        var levelLoc = LevelLocation.create((ServerLevel) entity.level(), entity.blockPosition());
        getPlayChannel().sendToNearbyPlayers(() -> levelLoc,
                new S2CMessagePlayerAnimation(entity.getId(), animation, hand));
    }

    public void sendToNearbyPlayers(Supplier<LevelLocation> supplier, CustomPacketPayload message) {
        var location = supplier.get();
        var pos = location.pos();
        PacketDistributor.sendToPlayersNear(location.level(), null, pos.x, pos.y, pos.z, location.range(), message);
    }

    public void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public void sendToAll(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }
}

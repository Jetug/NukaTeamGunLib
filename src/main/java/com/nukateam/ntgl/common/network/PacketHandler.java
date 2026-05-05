package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.network.message.NeoForgeNetwork;
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
    
//    private static final NeoForgeNetwork neoForgeNetwork = new NeoForgeNetwork();
//    
//    public static NeoForgeNetwork getPlayChannel() {
//        return neoForgeNetwork;
//    }

    public static void init() {
        var id = 0;
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(ResourceLocation.tryBuild(Ntgl.MOD_ID, "ntgl"), 1)
                .registerPlayMessage(String.valueOf(id++), C2SActionPacket.class, C2SActionPacket.CODEC, C2SActionPacket::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SGenericPacket.class, C2SGenericPacket.CODEC, C2SGenericPacket::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CInventoryPacket.class, S2CInventoryPacket.CODEC, S2CInventoryPacket::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateChassisConfig.class, S2CMessageUpdateChassisConfig.CODEC, S2CMessageUpdateChassisConfig::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateEquipmentConfig.class, S2CMessageUpdateEquipmentConfig.CODEC, S2CMessageUpdateEquipmentConfig::handle, PacketFlow.CLIENTBOUND)

                .registerPlayMessage(String.valueOf(id++), C2SMessageAim.class, C2SMessageAim.CODEC, C2SMessageAim::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageReload.class, C2SMessageReload.CODEC, C2SMessageReload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageShoot.class, C2SMessageShoot.CODEC, C2SMessageShoot::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageUnload.class, C2SMessageUnload.CODEC, C2SMessageUnload::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageReloadStop.class, C2SMessageReloadStop.CODEC, C2SMessageReloadStop::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageCraft.class, C2SMessageCraft.CODEC, C2SMessageCraft::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageAttachments.class, C2SMessageAttachments.CODEC, C2SMessageAttachments::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageChangeAmmo.class, C2SMessageChangeAmmo.CODEC, C2SMessageChangeAmmo::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageShooting.class, C2SMessageShooting.CODEC, C2SMessageShooting::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessagePreFireSound.class, C2SMessagePreFireSound.CODEC, C2SMessagePreFireSound::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageHandAction.class, C2SMessageHandAction.CODEC, C2SMessageHandAction::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageMeleeAttack.class, C2SMessageMeleeAttack.CODEC, C2SMessageMeleeAttack::handle, PacketFlow.SERVERBOUND)
                .registerPlayMessage(String.valueOf(id++), C2SMessageGrenade.class, C2SMessageGrenade.CODEC, C2SMessageGrenade::handle, PacketFlow.SERVERBOUND)

                .registerPlayMessage(String.valueOf(id++), S2CMessagePlayerAnimation.class, S2CMessagePlayerAnimation.CODEC, S2CMessagePlayerAnimation::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageEntityDeath.class, S2CMessageEntityDeath.CODEC, S2CMessageEntityDeath::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageEntityDeathFx.class, S2CMessageEntityDeathFx.CODEC, S2CMessageEntityDeathFx::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageStunGrenade.class, S2CMessageStunGrenade.CODEC, S2CMessageStunGrenade::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateWeapons.class, S2CMessageUpdateWeapons.CODEC, S2CMessageUpdateWeapons::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateAmmo.class, S2CMessageUpdateAmmo.CODEC, S2CMessageUpdateAmmo::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateAttachments.class, S2CMessageUpdateAttachments.CODEC, S2CMessageUpdateAttachments::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageBlood.class, S2CMessageBlood.CODEC, S2CMessageBlood::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageGunSound.class, S2CMessageGunSound.CODEC, S2CMessageGunSound::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitBlock.class, S2CMessageProjectileHitBlock.CODEC, S2CMessageProjectileHitBlock::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitEntity.class, S2CMessageProjectileHitEntity.CODEC, S2CMessageProjectileHitEntity::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileHitFluid.class, S2CMessageProjectileHitFluid.CODEC, S2CMessageProjectileHitFluid::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage(String.valueOf(id++), S2CMessageProjectileExplosion.class, S2CMessageProjectileExplosion.CODEC, S2CMessageProjectileExplosion::handle, PacketFlow.CLIENTBOUND)

                .registerPlayMessage(String.valueOf(id++), S2CMessageUpdateEntityData.class, S2CMessageUpdateEntityData.CODEC, S2CMessageUpdateEntityData::handle, PacketFlow.CLIENTBOUND)
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

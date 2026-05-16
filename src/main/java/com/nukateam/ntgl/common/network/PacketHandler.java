package com.nukateam.ntgl.common.network;


import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.network.message.chassis.*;
import com.nukateam.ntgl.common.network.message.weapon.*;
import com.nukateam.ntgl.modules.data.message.S2CMessageUpdateEntityData;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.function.Supplier;
@EventBusSubscriber(modid = Ntgl.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final NeoForgeNetwork neoForgeNetwork = new NeoForgeNetwork();

    public static NeoForgeNetwork getPlayChannel() {
        return neoForgeNetwork;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(C2SActionPacket.TYPE, C2SActionPacket.CODEC, C2SActionPacket::handle);
        registrar.playToServer(C2SGenericPacket.TYPE, C2SGenericPacket.CODEC, C2SGenericPacket::handle);

        registrar.playToClient(S2CInventoryPacket.TYPE, S2CInventoryPacket.CODEC, S2CInventoryPacket::handle);
        registrar.playToClient(S2CMessageUpdateChassisConfig.TYPE, S2CMessageUpdateChassisConfig.CODEC, S2CMessageUpdateChassisConfig::handle);
        registrar.playToClient(S2CMessageUpdateEquipmentConfig.TYPE, S2CMessageUpdateEquipmentConfig.CODEC, S2CMessageUpdateEquipmentConfig::handle);

        registrar.playToServer(C2SMessageAim.TYPE, C2SMessageAim.CODEC, C2SMessageAim::handle);
        registrar.playToServer(C2SMessageReload.TYPE, C2SMessageReload.CODEC, C2SMessageReload::handle);
        registrar.playToServer(C2SMessageShoot.TYPE, C2SMessageShoot.CODEC, C2SMessageShoot::handle);
        registrar.playToServer(C2SMessageUnload.TYPE, C2SMessageUnload.CODEC, C2SMessageUnload::handle);
        registrar.playToServer(C2SMessageReloadStop.TYPE, C2SMessageReloadStop.CODEC, C2SMessageReloadStop::handle);
        registrar.playToServer(C2SMessageCraft.TYPE, C2SMessageCraft.CODEC, C2SMessageCraft::handle);
        registrar.playToServer(C2SMessageAttachments.TYPE, C2SMessageAttachments.CODEC, C2SMessageAttachments::handle);
        registrar.playToServer(C2SMessageChangeAmmo.TYPE, C2SMessageChangeAmmo.CODEC, C2SMessageChangeAmmo::handle);
        registrar.playToServer(C2SMessageShooting.TYPE, C2SMessageShooting.CODEC, C2SMessageShooting::handle);
        registrar.playToServer(C2SMessagePreFireSound.TYPE, C2SMessagePreFireSound.CODEC, C2SMessagePreFireSound::handle);
        registrar.playToServer(C2SMessageHandAction.TYPE, C2SMessageHandAction.CODEC, C2SMessageHandAction::handle);
        registrar.playToServer(C2SMessageMeleeAttack.TYPE, C2SMessageMeleeAttack.CODEC, C2SMessageMeleeAttack::handle);
        registrar.playToServer(C2SMessageGrenade.TYPE, C2SMessageGrenade.CODEC, C2SMessageGrenade::handle);

        registrar.playToClient(S2CMessagePlayerAnimation.TYPE, S2CMessagePlayerAnimation.CODEC, S2CMessagePlayerAnimation::handle);
        registrar.playToClient(S2CMessageEntityDeath.TYPE, S2CMessageEntityDeath.CODEC, S2CMessageEntityDeath::handle);
        registrar.playToClient(S2CMessageEntityDeathFx.TYPE, S2CMessageEntityDeathFx.CODEC, S2CMessageEntityDeathFx::handle);
        registrar.playToClient(S2CMessageStunGrenade.TYPE, S2CMessageStunGrenade.CODEC, S2CMessageStunGrenade::handle);

        registrar.playToClient(S2CMessageUpdateWeapons.TYPE, S2CMessageUpdateWeapons.CODEC, S2CMessageUpdateWeapons::handle);
        registrar.playToClient(S2CMessageUpdateAmmo.TYPE, S2CMessageUpdateAmmo.CODEC, S2CMessageUpdateAmmo::handle);
        registrar.playToClient(S2CMessageUpdateProjectiles.TYPE, S2CMessageUpdateProjectiles.CODEC, S2CMessageUpdateProjectiles::handle);
        registrar.playToClient(S2CMessageUpdateAttachments.TYPE, S2CMessageUpdateAttachments.CODEC, S2CMessageUpdateAttachments::handle);

        registrar.playToClient(S2CMessageBlood.TYPE, S2CMessageBlood.CODEC, S2CMessageBlood::handle);
        registrar.playToClient(S2CMessageGunSound.TYPE, S2CMessageGunSound.CODEC, S2CMessageGunSound::handle);
        registrar.playToClient(S2CMessageProjectileHitBlock.TYPE, S2CMessageProjectileHitBlock.CODEC, S2CMessageProjectileHitBlock::handle);
        registrar.playToClient(S2CMessageProjectileHitEntity.TYPE, S2CMessageProjectileHitEntity.CODEC, S2CMessageProjectileHitEntity::handle);
        registrar.playToClient(S2CMessageProjectileHitFluid.TYPE, S2CMessageProjectileHitFluid.CODEC, S2CMessageProjectileHitFluid::handle);
        registrar.playToClient(S2CMessageProjectileExplosion.TYPE, S2CMessageProjectileExplosion.CODEC, S2CMessageProjectileExplosion::handle);

        registrar.playToClient(S2CMessageUpdateEntityData.TYPE, S2CMessageUpdateEntityData.CODEC, S2CMessageUpdateEntityData::handle);
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

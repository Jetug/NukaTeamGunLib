package com.nukateam.ntgl.common.network;

import com.nukateam.ntgl.common.network.LevelLocation;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Supplier;

public class NeoForgeNetwork {
    public void send(Connection connection, CustomPacketPayload message) {
        if (connection.getSending().isServerbound()) {
            connection.send(new ServerboundCustomPayloadPacket(message));
        } else {
            connection.send(new ClientboundCustomPayloadPacket(message));
        }
    }

    public void sendToPlayer(Supplier<ServerPlayer> supplier, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(supplier.get(), message);
    }

    public void sendToTrackingEntity(Supplier<Entity> supplier, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersTrackingEntity(supplier.get(), message);
    }

    public void sendToTrackingBlockEntity(Supplier<BlockEntity> supplier, CustomPacketPayload message) {
        this.sendToTrackingChunk(() -> {
            BlockEntity entity = (BlockEntity)supplier.get();
            return entity.getLevel().getChunkAt(entity.getBlockPos());
        }, message);
    }

    public void sendToTrackingLocation(Supplier<LevelLocation> supplier, CustomPacketPayload message) {
        this.sendToTrackingChunk(() -> {
            LevelLocation location = supplier.get();
            Vec3 pos = location.pos();
            int chunkX = SectionPos.blockToSectionCoord(pos.x);
            int chunkZ = SectionPos.blockToSectionCoord(pos.z);
            return location.level().getChunk(chunkX, chunkZ);
        }, message);
    }

    public void sendToTrackingChunk(Supplier<LevelChunk> supplier, CustomPacketPayload message) {
        LevelChunk chunk = supplier.get();
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel)chunk.getLevel(), chunk.getPos(), message);
    }

    public void sendToNearbyPlayers(Supplier<LevelLocation> supplier, CustomPacketPayload message) {
        LevelLocation location = supplier.get();
        Vec3 pos = location.pos();
        PacketDistributor.sendToPlayersNear(location.level(), null, pos.x, pos.y, pos.z, location.range(), message);
    }

    public void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public void sendToAll(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }
}

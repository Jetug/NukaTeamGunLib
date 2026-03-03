package com.nukateam.ntgl.mixin.chassis.client;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientGamePacketListener {
    @Shadow
    @Final
    private static Logger LOGGER;
//    @Shadow
//    @Final
//    private Minecraft minecraft;
    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private Map<UUID, PlayerInfo> playerInfoMap;


    @Inject(method = "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V", at = @At("HEAD"), cancellable = true, remap=false)
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket, CallbackInfo ci) {
        var minecraft = Minecraft.getInstance();
        PacketUtils.ensureRunningOnSameThread(pPacket, this, minecraft);
        var entity = level.getEntity(pPacket.getVehicle());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
        } else {
            var flag = entity.hasIndirectPassenger(minecraft.player);
            entity.ejectPassengers();

            for (int i : pPacket.getPassengers()) {
                var entity1 = level.getEntity(i);
                if (entity1 != null) {
                    entity1.startRiding(entity, true);
                    if (entity1 == minecraft.player && !flag) {
                        if (entity instanceof Boat) {
                            minecraft.player.yRotO = entity.getYRot();
                            minecraft.player.setYRot(entity.getYRot());
                            minecraft.player.setYHeadRot(entity.getYRot());
                        }
                        if (!(entity instanceof WearableChassis))
                            minecraft.gui.setOverlayMessage(Component.translatable("mount.onboard", minecraft.options.keyShift.getTranslatedKeyMessage()), false);
                    }
                }
            }
        }
        ci.cancel();
    }

//    @Inject(method = "handlePlayerInfo(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoPacket;)V", at = @At("HEAD"))
//    public void handlePlayerInfo(ClientboundPlayerInfoPacket pPacket, CallbackInfo ci) {
//        for(PlayerUpdate playerUpdate : pPacket.getEntries()) {
//            if (pPacket.getAction() == Action.REMOVE_PLAYER) {
//
//            } else if (pPacket.getAction() == Action.ADD_PLAYER){
//                PlayerInfo playerinfo = this.playerInfoMap.get(playerUpdate.getProfile().getId());
//                playerinfo = new PlayerInfo(playerUpdate);
//                this.playerInfoMap.put(playerinfo.getProfile().getId(), playerinfo);
//                this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
//
//            }
//        }
//    }
}

package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.framework.api.network.MessageDirection;
import com.nukateam.chassis_core.common.network.packet.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    public static final SimpleChannel PLAY_CHANNEL = NetworkRegistry.ChannelBuilder.named(ResourceLocation.tryBuild(Ntgl.MOD_ID, "play"))
            .networkProtocolVersion(() -> "1")
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static ForgeNetwork getPlayChannel() {
        return new ForgeNetwork(PLAY_CHANNEL);
    }

    public static void init() {
        //CHASSIS CORE
        registerPlayMessage(C2SActionPacket.class   , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SGenericPacket.class  , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(S2CInventoryPacket.class                , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageUpdateChassisConfig.class     , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageUpdateEquipmentConfig.class   , NetworkDirection.PLAY_TO_CLIENT);

        //NTGL
        registerPlayMessage(C2SMessageAim.class         , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageReload.class      , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageShoot.class       , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageUnload.class      , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageCraft.class       , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageAttachments.class , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageShooting.class    , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessagePreFireSound.class, NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageHandAction.class  , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageMeleeAttack.class , NetworkDirection.PLAY_TO_SERVER);
        registerPlayMessage(C2SMessageGrenade.class     , NetworkDirection.PLAY_TO_SERVER);

        registerPlayMessage(S2CMessagePlayerAnimation.class     , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageEntityDeath.class         , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageEntityDeathFx.class       , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageReload.class              , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageStunGrenade.class         , NetworkDirection.PLAY_TO_CLIENT);

        registerPlayMessage(S2CMessageUpdateWeapons.class       , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageUpdateAmmo.class          , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageUpdateAttachments.class   , NetworkDirection.PLAY_TO_CLIENT);

        registerPlayMessage(S2CMessageBlood.class               , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageGunSound.class            , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageProjectileHitBlock.class  , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageProjectileHitEntity.class , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageProjectileHitFluid.class  , NetworkDirection.PLAY_TO_CLIENT);
        registerPlayMessage(S2CMessageProjectileExplosion.class , NetworkDirection.PLAY_TO_CLIENT);

    }

    public static  <T extends IMessage<T>> void registerPlayMessage(Class<T> messageClass, @Nullable NetworkDirection direction) {
        try {
            var constructor = messageClass.getDeclaredConstructor();
            var message = constructor.newInstance();

            PLAY_CHANNEL.registerMessage(packetId++,
                    messageClass, message::encode, message::decode,
                    (msg, messageContext) -> {
                        message.handle(msg, messageContext.get());
                    },
                    Optional.ofNullable(direction));

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("The message %s is missing an empty parameter constructor", messageClass.getName()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Unable to access the constructor of %s. Make sure the constructor is public.", messageClass.getName()), e);
        } catch (InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void sendAnimation(LivingEntity entity, InteractionHand hand, AnimationType animation) {
        var levelLoc = LevelLocation.create(entity.level(), entity.blockPosition());
        getPlayChannel().sendToNearbyPlayers(() -> levelLoc,
                new S2CMessagePlayerAnimation(entity.getId(), animation, hand));
    }
}

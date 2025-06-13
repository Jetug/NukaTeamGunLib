package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.client.config.*;
import com.nukateam.ntgl.client.util.*;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.common.base.NetworkAmmoManager;
import com.nukateam.ntgl.common.base.NetworkAttachmentManager;
import com.nukateam.ntgl.common.base.NetworkGunManager;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.foundation.particles.*;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.createDeathEffectClient;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler {
    public static void handleMessageGunSound(S2CMessageGunSound message) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (message.showMuzzleFlash())
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.getShooterId());

        if (message.getShooterId() == mc.player.getId()) {
            mc.getSoundManager().play(new SimpleSoundInstance(message.getId(), SoundSource.PLAYERS,
                    GunShotSound.getVolume(message.getVolume()), message.getPitch(),
                    mc.level.getRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
        else {
            mc.getSoundManager().play(new GunShotSound(message.getId(), SoundSource.PLAYERS,
                    message.getX(), message.getY(), message.getZ(),
                    message.getVolume(), message.getPitch(), message.isReload()));
        }
    }

    public static void handleMessageBlood(S2CMessageBlood message) {
        if (!Config.CLIENT.particle.enableBlood.get())
            return;

        var world = Minecraft.getInstance().level;
        if (world != null) {
            for (int i = 0; i < 10; i++) {
                world.addParticle(
                        ModParticleTypes.BLOOD.get(), true,
                        message.getX(), message.getY(), message.getZ(),
                        0.5, 0, 0.5);
            }
        }
    }

    public static void handleMessageBulletTrail(S2CMessageBulletTrail message) {
        var world = Minecraft.getInstance().level;
        if (world != null) {
            var entityIds = message.getEntityIds();
            var positions = message.getPositions();
            var motions = message.getMotions();
            var item = message.getItem();
            var trailColor = message.getTrailColor();
            var trailLengthMultiplier = message.getTrailLengthMultiplier();
            var life = message.getLife();
            var gravity = message.getGravity();
            var shooterId = message.getShooterId();
            var enchanted = message.isEnchanted();
            var data = message.getParticleData();

            for (int i = 0; i < message.getCount(); i++) {
                BulletTrailRenderingHandler.get().add(
                        new BulletTrail(entityIds[i], positions[i], motions[i],
                                item, trailColor, trailLengthMultiplier, life,
                                gravity, shooterId, enchanted, data)
                );
            }
        }
    }

    public static void handleExplosionStunGrenade(S2CMessageStunGrenade message) {
        var mc = Minecraft.getInstance();
        var particleManager = mc.particleEngine;
        var world = mc.level;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        /* Spawn lingering smoke particles */
        for (int i = 0; i < 30; i++) {
            spawnParticle(particleManager, ParticleTypes.CLOUD, x, y, z, world.random, 0.2);
        }

        /* Spawn fast moving smoke/spark particles */
        for (int i = 0; i < 30; i++) {
            var smoke = spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, world.random, 4.0);
            smoke.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.4)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, world.random, 4.0);
        }
    }

    private static Particle spawnParticle(ParticleEngine manager, ParticleOptions data, double x, double y, double z, RandomSource rand, double velocityMultiplier) {
        return manager.createParticle(data, x, y, z, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier);
    }

    public static void handleProjectileHitBlock(S2CMessageProjectileHitBlock message) {
        var mc = Minecraft.getInstance();
        var world = mc.level;

        if (world != null) {
            var state = world.getBlockState(message.getPos());
            var holeX = message.getX() + 0.005 * message.getFace().getStepX();
            var holeY = message.getY() + 0.005 * message.getFace().getStepY();
            var holeZ = message.getZ() + 0.005 * message.getFace().getStepZ();
            var distance = Math.sqrt(mc.player.distanceToSqr(message.getX(), message.getY(), message.getZ()));

            world.addParticle(
                    new BulletHoleData(message.getFace(), message.getPos()),
                    false, holeX, holeY, holeZ, 0, 0, 0
            );

            if (distance < Config.CLIENT.particle.impactParticleDistance.get()) {
                for (int i = 0; i < 4; i++) {
                    var normal = message.getFace().getNormal();
                    var motion = new Vec3(normal.getX(), normal.getY(), normal.getZ());
                    motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));

                    world.addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            false, message.getX(), message.getY(),
                            message.getZ(), motion.x, motion.y, motion.z);
                }
            }

            if (distance <= Config.CLIENT.sounds.impactSoundDistance.get()) {
//                float volume = (float) (1.0F - (distance / Config.CLIENT.sounds.impactSoundDistance.get()));
//                volume = Math.max(volume, 0.0F);

                world.playLocalSound(message.getX(), message.getY(), message.getZ(),
                        state.getSoundType().getBreakSound(), SoundSource.BLOCKS,
                        1.0F, 2.0F, false);
            }
        }
    }

    public static void handleEntityData(S2CMessageEntityData message) {
//        var mc = Minecraft.getInstance();
//        var level = mc.level;
//
//        if (level != null) {
//            var entity = level.getEntity(message.getEntityId());
//            if(entity instanceof ProjectileEntity projectile) {
//                projectile.readAdditionalSaveData(message.getData());
//                projectile.setClientUpdated();
//            }
//        }
    }

    public static void handleEntityDeathFx(S2CMessageEntityDeathFx message) {
        var mc = Minecraft.getInstance();
        var level = mc.level;

        if (level != null) {
            var entity = (LivingEntity)level.getEntity(message.getEntityId());
            createDeathEffectClient(entity, message.getData());
        }
    }

    private static double getRandomDir(RandomSource random) {
        return -0.25 + random.nextDouble() * 0.5;
    }

    public static void handleProjectileHitEntity(S2CMessageProjectileHitEntity message) {
        var mc = Minecraft.getInstance();
        var world = mc.level;

        if (world == null) return;
        var event = getHitSound(message.isCritical(), message.isHeadshot(), message.isPlayer());
        if (event == null) return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 0.8F + world.random.nextFloat() * 0.2F));
    }

    @Nullable
    private static SoundEvent getHitSound(boolean critical, boolean headshot, boolean player) {
        if (critical) {
            if (Config.CLIENT.sounds.playSoundWhenCritical.get()) {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.criticalSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_CRIT;
            }
        } else if (headshot) {
            if (Config.CLIENT.sounds.playSoundWhenHeadshot.get()) {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.headshotSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_KNOCKBACK;
            }
        } else if (player) {
            return SoundEvents.PLAYER_HURT;
        }
        return null;
    }

    public static void handleRemoveProjectile(S2CMessageRemoveProjectile message) {
        BulletTrailRenderingHandler.get().remove(message.getEntityId());
    }

    public static void handleUpdateGuns(S2CMessageUpdateGuns message) {
        NetworkGunManager.updateRegisteredGuns(message);
        CustomGunManager.updateCustomGuns(message);
    }

    public static void handleUpdateAmmo(S2CMessageUpdateAmmo message) {
        NetworkAmmoManager.updateRegisteredAmmo(message);
        CustomAmmoManager.updateCustomAmmo(message);
    }

    public static void handleUpdateAttachments(S2CMessageUpdateAttachments message) {
        NetworkAttachmentManager.updateRegisteredAttachments(message);
    }

    public static void handleReload(S2CMessageReload message) {
        var player = Minecraft.getInstance().player;
        if (player != null && !player.isSpectator()) {
            var arm = message.isRightHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            var dataKey = message.isRightHand() ?
                    ModSyncedDataKeys.RELOADING_RIGHT : ModSyncedDataKeys.RELOADING_LEFT;

            ClientReloadHandler.get().setReloading(!dataKey.getValue(player), arm);
        }
    }
}

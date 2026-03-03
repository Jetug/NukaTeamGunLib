package com.nukateam.ntgl.common.util.trackers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.event.MeleeAttackEvent;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.util.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.internal.runtime.regexp.joni.constants.TargetInfo;

import java.util.*;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class MeleeTracker {
    private static final Map<Pair<InteractionHand, LivingEntity>, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        try {
            if (!event.getEntity().level().isClientSide) {
                var player = event.getEntity();
                handTick(player, InteractionHand.MAIN_HAND);
                handTick(player, InteractionHand.OFF_HAND);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }


    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        try {
            for (var key : TRACKER_MAP.keySet()) {
                var entity = key.getSecond();
                if (entity instanceof Player) continue;

                handTick(entity, InteractionHand.MAIN_HAND);
                handTick(entity, InteractionHand.OFF_HAND);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null) {
            TRACKER_MAP.remove(new Pair<InteractionHand, LivingEntity>(InteractionHand.MAIN_HAND, event.getEntity()));
            TRACKER_MAP.remove(new Pair<InteractionHand, LivingEntity>(InteractionHand.OFF_HAND, event.getEntity()));
        }
    }

    public static void start(WeaponData data, InteractionHand hand){
        var dataKey = ModSyncedDataKeys.getMeleeKey(hand);
        dataKey.setValue(data.wielder, true);
        addTracker(data, hand);
    }

    private static void addTracker(WeaponData data, InteractionHand hand) {
        assert data.weapon != null && data.wielder != null;
        var dataKey = ModSyncedDataKeys.getMeleeKey(hand);
        var entity = data.wielder;
        var key = new Pair<>(hand, entity);

        if (!TRACKER_MAP.containsKey(key)) {
            if (!(data.weapon.getItem() instanceof IWeapon)) {
                dataKey.setValue(entity, false);
                return;
            }
            TRACKER_MAP.put(key, new Tracker(data, hand));
            PacketHandler.sendAnimation(entity, hand, AnimationType.MELEE);
        }
    }

    private static void stopMelee(LivingEntity entity, InteractionHand hand) {
        var dataKey = ModSyncedDataKeys.getMeleeKey(hand);
        TRACKER_MAP.remove(new Pair<>(hand, entity));
        dataKey.setValue(entity, false);
    }

    private static void handTick(LivingEntity entity, InteractionHand hand) {
        var key = new Pair<>(hand, entity);
        var tracker = TRACKER_MAP.get(key);
        if (tracker != null){
            if (!tracker.isSameWeapon(entity)) {
                stopMelee(entity, hand);
            }

            if (tracker.meleeTick > 0)
                tracker.meleeTick--;

            if (tracker.meleeTick == tracker.cooldown) {
                tracker.tryMeleeAttack(tracker.data);
            }

            if (tracker.meleeTick == 0) {
                stopMelee(entity, hand);
            }
        }
    }

    private static class Tracker{
        private final InteractionHand hand;
        private final ItemStack stack;

        private final int cooldown;
        private final WeaponData data;
        private int meleeTick;
        private final int attackDelay;
        private final float meleeDamage;
        private final float knockback;
        private final double attackDistance;
        private final double attackAngle;
        private final int maxTargets;

        private Tracker(WeaponData data, InteractionHand hand) {
            this.hand = hand;
            this.data = data;
            this.stack = data.weapon;
            assert stack != null;
            this.cooldown = WeaponModifierHelper.getMeleeCooldown(data);
            this.attackDelay = WeaponModifierHelper.getMeleeDelay(data);
            this.meleeTick = attackDelay + cooldown;
            this.meleeDamage = WeaponModifierHelper.getMeleeDamage(data);
            this.attackDistance = WeaponModifierHelper.getMeleeDistance(data);
            this.attackAngle = WeaponModifierHelper.getMeleeAngle(data);
            this.knockback = WeaponModifierHelper.getMeleeKnockback(data);
            this.maxTargets = WeaponModifierHelper.getMeleeMaxTargets(data);
        }

        private void tryMeleeAttack(WeaponData weaponData) {
            assert weaponData.wielder != null && weaponData.weapon != null;
            var wielder = weaponData.wielder;
            var targets = getTargets(wielder);

            var targetsToAttack = new ArrayList<LivingEntity>();
            var limit = maxTargets > 0 ? maxTargets : Integer.MAX_VALUE;

            for (int i = 0; i < Math.min(limit, targets.size()); i++) {
                var target = targets.get(i).entity;
                if(target != wielder.getVehicle()) {
                    targetsToAttack.add(target);
                }
            }

            if (!NeoForge.EVENT_BUS.post(new MeleeAttackEvent.Pre(wielder, weaponData, hand, targetsToAttack)).isCanceled()) {
                if (!targetsToAttack.isEmpty()) {
                    for (var target : targetsToAttack) {
                        if(wielder.getVehicle() != target) {
                            attackEntity(wielder, target);
                        }
                    }

                    playAttackSound(wielder);
                    spawnAttackEffects(wielder, targetsToAttack);
                    NeoForge.EVENT_BUS.post(new MeleeAttackEvent.Post(wielder, weaponData, hand, targetsToAttack));
                }
            }
        }

        private @NotNull ArrayList<MeleeTracker.TargetInfo> getTargets(LivingEntity player) {
            var playerPos = player.getEyePosition(1.0F);
            var lookVec = player.getLookAngle().normalize();
            var coneAngleCos = Math.cos(Math.toRadians(attackAngle / 2));
            var visibleTargets = new ArrayList<MeleeTracker.TargetInfo>();
            var area = player.getBoundingBox().inflate(attackDistance);

            for (var entity : player.level().getEntities(player, area)) {
                if (!(entity instanceof LivingEntity living) ||
                        !entity.isAttackable() ||
                        entity.isAlliedTo(player)) continue;

                var closestPoint = findClosestPointOnHitbox(playerPos, lookVec, living);
                var distance = playerPos.distanceTo(closestPoint);

                if (distance > attackDistance || !isInAttackCone(playerPos, lookVec, closestPoint, coneAngleCos))
                    continue;

                if (!isVisible(playerPos, closestPoint, player))
                    continue;

                visibleTargets.add(new MeleeTracker.TargetInfo(living, distance, closestPoint));
            }

            visibleTargets.sort(Comparator.comparingDouble(t -> t.distance));
            return visibleTargets;
        }

        private void attackEntity(LivingEntity shooter, Entity target) {
            if(shooter instanceof Player player) {
                target.hurt(shooter.damageSources().playerAttack(player), meleeDamage);
            }
            else target.hurt(shooter.damageSources().mobAttack(shooter), meleeDamage);

            var knockbackVec = new Vec3(
                    target.getX() - shooter.getX(),
                    0,
                    target.getZ() - shooter.getZ()
            ).normalize().scale(knockback);

            target.push(knockbackVec.x, knockbackVec.y + 0.2, knockbackVec.z);
            target.hurtMarked = true;
        }

        private Vec3 findClosestPointOnHitbox(Vec3 start, Vec3 direction, LivingEntity target) {
            var hitbox = target.getBoundingBox();
            var center = hitbox.getCenter();
            var toCenter = center.subtract(start);
            var projectionLength = toCenter.dot(direction);
            var projectedPoint = start.add(direction.scale(projectionLength));

            var x = Mth.clamp(projectedPoint.x, hitbox.minX, hitbox.maxX);
            var y = Mth.clamp(projectedPoint.y, hitbox.minY, hitbox.maxY);
            var z = Mth.clamp(projectedPoint.z, hitbox.minZ, hitbox.maxZ);

            return new Vec3(x, y, z);
        }

        private boolean isSameWeapon(LivingEntity entity) {
            return !this.stack.isEmpty() && entity.getItemInHand(hand) == this.stack;
        }

        private boolean isInAttackCone(Vec3 playerPos, Vec3 lookVec, Vec3 point, double coneAngleCos) {
            var toPoint = point.subtract(playerPos).normalize();
            return lookVec.dot(toPoint) >= coneAngleCos;
        }

        private boolean isVisible(Vec3 start, Vec3 end, LivingEntity entity) {
            var level = entity.level();
            if (level.isClientSide()) return true;

            var context = new ClipContext(start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity);

            return level.clip(context).getType() == HitResult.Type.MISS;
        }

        private void playAttackSound(LivingEntity shooter) {
            shooter.level().playSound(
                    null,
                    shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F + shooter.getRandom().nextFloat() * 0.2F
            );
        }

        private void spawnAttackEffects(LivingEntity player, List<LivingEntity> targets) {
            if(player.level() instanceof ServerLevel serverLevel) {

                Vec3 lookVec = player.getLookAngle().scale(attackDistance / 2);
                serverLevel.sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        player.getX() + lookVec.x,
                        player.getY() + 1.0,
                        player.getZ() + lookVec.z,
                        10,
                        0.5, 0.5, 0.5,
                        0.0
                );


                for(LivingEntity target : targets) {
                    serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            target.getX(),
                            target.getY() + target.getBbHeight() / 2,
                            target.getZ(),
                            5,
                            0.2, 0.2, 0.2,
                            0.1
                    );
                }
            }
        }
    }

    private record TargetInfo(LivingEntity entity, double distance, Vec3 hitPoint) {}
}

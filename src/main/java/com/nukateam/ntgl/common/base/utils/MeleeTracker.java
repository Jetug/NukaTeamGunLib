package com.nukateam.ntgl.common.base.utils;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class MeleeTracker {
    private static final Map<LivingEntity, MeleeTracker> TRACKER_MAP = new HashMap<>();

    private final int startTick;
    private final LivingEntity shooter;
    private final InteractionHand arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final Gun gun;

    private int cooldown = 5;
    private int meleeTick = 5;
    private int attackDelay = 4;
    private float meleeDamage = 6;
    private float knockback = 0.2f;
    private double attackDistance = 3;
    private double attackAngle = 180;
    private int maxTargets = 6;

    private MeleeTracker(LivingEntity entity, InteractionHand arm) {
        this.startTick = entity.tickCount;
        this.arm = arm;
        this.stack = entity.getItemInHand(arm);
        this.gunItem = ((GunItem) stack.getItem());
        this.gun = gunItem.getModifiedGun(stack);
        this.shooter = entity;

        var data = new GunData(stack, entity);
        this.cooldown = GunModifierHelper.getMeleeCooldown(data);
        this.attackDelay = GunModifierHelper.getMeleeDelay(data);
        this.meleeTick = attackDelay + cooldown;
        this.meleeDamage = GunModifierHelper.getMeleeDamage(data);
        this.attackDistance = GunModifierHelper.getMeleeDistance(data);
        this.attackAngle = GunModifierHelper.getMeleeAngle(data);
        this.knockback = GunModifierHelper.getMeleeKnockback(data);
        this.maxTargets = GunModifierHelper.getMeleeMaxTargets(data);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                onEntityTick(player);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
                for (var entity: TRACKER_MAP.keySet()) {
                    if(entity instanceof Player) continue;
                    onEntityTick(entity);
                }
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
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }

    private boolean isSameWeapon(LivingEntity entity) {
        return !this.stack.isEmpty() && entity.getItemInHand(arm) == this.stack;
    }

    private static void onEntityTick(LivingEntity entity) {
        if (ModSyncedDataKeys.MELEE_RIGHT.getValue(entity)) {
            handTick(entity, InteractionHand.MAIN_HAND);
        }
        else if (ModSyncedDataKeys.MELEE_LEFT.getValue(entity)) {
            handTick(entity, InteractionHand.OFF_HAND);
        }
        else if (TRACKER_MAP.containsKey(entity)) {
            TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity shooter, InteractionHand arm) {
        var tracker = TRACKER_MAP.get(shooter);
        var isSameWeapon = !tracker.isSameWeapon(shooter);

        if (isSameWeapon) {
            TRACKER_MAP.remove(shooter);
            var reloadKey = getDataKey(arm);
            reloadKey.setValue(shooter, false);
        }

        if(tracker.meleeTick > 0)
            tracker.meleeTick--;

        if(tracker.meleeTick == tracker.cooldown){
            tracker.tryMeleeAttack(shooter, tracker.stack);
        }

        if(tracker.meleeTick == 0){
            stopMelee(shooter, arm);
        }
    }

    private boolean tryMeleeAttack(LivingEntity player, ItemStack stack) {
        Vec3 playerPos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle().normalize();
        double coneAngleCos = Math.cos(Math.toRadians(attackAngle / 2));


        List<TargetInfo> visibleTargets = new ArrayList<>();

        AABB area = player.getBoundingBox().inflate(attackDistance);
        for (Entity entity : player.level().getEntities(player, area)) {
            if (!(entity instanceof LivingEntity living) ||
                    !entity.isAttackable() ||
                    entity.isAlliedTo(player)) continue;


            Vec3 closestPoint = findClosestPointOnHitbox(playerPos, lookVec, living);
            double distance = playerPos.distanceTo(closestPoint);


            if (distance > attackDistance || !isInAttackCone(playerPos, lookVec, closestPoint, coneAngleCos))
                continue;


            if (!isVisible(playerPos, closestPoint, player.level()))
                continue;

            visibleTargets.add(new TargetInfo(living, distance, closestPoint));
        }


        visibleTargets.sort(Comparator.comparingDouble(t -> t.distance));


        List<LivingEntity> targetsToAttack = new ArrayList<>();
        int limit = maxTargets > 0 ? maxTargets : Integer.MAX_VALUE;
        for (int i = 0; i < Math.min(limit, visibleTargets.size()); i++) {
            targetsToAttack.add(visibleTargets.get(i).entity);
        }

        if (!targetsToAttack.isEmpty()) {
            for (LivingEntity target : targetsToAttack) {
                performMeleeAttack(player, target);
            }

            playAttackSound(player);
            spawnAttackEffects(player, targetsToAttack);
            return true;
        }
        return false;
    }


    private Vec3 findClosestPointOnHitbox(Vec3 start, Vec3 direction, LivingEntity target) {
        AABB hitbox = target.getBoundingBox();


        Vec3 center = hitbox.getCenter();
        Vec3 toCenter = center.subtract(start);
        double projectionLength = toCenter.dot(direction);
        Vec3 projectedPoint = start.add(direction.scale(projectionLength));


        double x = Mth.clamp(projectedPoint.x, hitbox.minX, hitbox.maxX);
        double y = Mth.clamp(projectedPoint.y, hitbox.minY, hitbox.maxY);
        double z = Mth.clamp(projectedPoint.z, hitbox.minZ, hitbox.maxZ);

        return new Vec3(x, y, z);
    }


    private boolean isInAttackCone(Vec3 playerPos, Vec3 lookVec, Vec3 point, double coneAngleCos) {
        Vec3 toPoint = point.subtract(playerPos).normalize();
        return lookVec.dot(toPoint) >= coneAngleCos;
    }


    private boolean isVisible(Vec3 start, Vec3 end, Level level) {
        if (level.isClientSide()) return true;

        ClipContext context = new ClipContext(start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null);
        return level.clip(context).getType() == HitResult.Type.MISS;
    }


    private static class TargetInfo {
        final LivingEntity entity;
        final double distance;
        final Vec3 hitPoint;

        TargetInfo(LivingEntity entity, double distance, Vec3 hitPoint) {
            this.entity = entity;
            this.distance = distance;
            this.hitPoint = hitPoint;
        }
    }

    private void performMeleeAttack(LivingEntity shooter, Entity target) {
        if(shooter instanceof Player player) {
            target.hurt(shooter.damageSources().playerAttack(player), meleeDamage);
        }
        else target.hurt(shooter.damageSources().mobAttack(shooter), meleeDamage);

        Vec3 knockbackVec = new Vec3(
                target.getX() - shooter.getX(),
                0,
                target.getZ() - shooter.getZ()
        ).normalize().scale(knockback);

        target.push(knockbackVec.x, knockbackVec.y + 0.2, knockbackVec.z);
        target.hurtMarked = true;
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

//    private static void resetTracker(MeleeTracker tracker, GunData data) {
//        tracker.meleeTick = GunModifierHelper.getReloadTime(data);
//    }

    public static void start(LivingEntity entity, InteractionHand arm){
        var reloadKey = getDataKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static SyncedDataKey<LivingEntity, Boolean> getDataKey(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.MELEE_RIGHT : ModSyncedDataKeys.MELEE_LEFT;
    }

    private static boolean addTracker(LivingEntity entity, InteractionHand arm) {
        var reloadKey = getDataKey(arm);

        var gunItem = entity.getItemInHand(arm).getItem();

        if (!TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof GunItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            TRACKER_MAP.put(entity, new MeleeTracker(entity, arm));
        }
        return false;
    }

    private static void stopMelee(LivingEntity entity, InteractionHand arm) {
        var dataKey = getDataKey(arm);
        TRACKER_MAP.remove(entity);
        dataKey.setValue(entity, false);
    }
}

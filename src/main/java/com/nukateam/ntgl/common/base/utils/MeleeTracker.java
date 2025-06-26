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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static com.nukateam.ntgl.common.util.util.LivingEntityUtils.getInteractionHand;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class MeleeTracker {
    private static final Map<LivingEntity, MeleeTracker> TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final LivingEntity shooter;
    private final HumanoidArm arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final Gun gun;

    public int meleeTick;
    public int attackDelay;
    public boolean isEnd = false;

    private MeleeTracker(LivingEntity entity, HumanoidArm arm) {
        this.startTick = entity.tickCount;
        this.arm = arm;
        this.stack = entity.getItemInHand(getInteractionHand(arm));
        this.gunItem = ((GunItem) stack.getItem());
        this.gun = gunItem.getModifiedGun(stack);
        this.shooter = entity;

        var data = new GunData(stack, entity);
        meleeTick = GunModifierHelper.getMeleeDuration(data);
        attackDelay = GunModifierHelper.getMeleeDelay(data);

//        var loadingType = GunModifierHelper.getLoadingType(data);
//        if(loadingType == LoadingType.PER_CARTRIDGE){
//            ModSyncedDataKeys.RELOAD_START.setValue(entity, true);
//            meleeTick = GunModifierHelper.getReloadStart(data);
//            isStart = true;
//        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                handTick(player);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START) {
                for (var entity: TRACKER_MAP.keySet()) {
                    if(entity instanceof Player) continue;
                    handTick(entity);
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
        if(arm == HumanoidArm.RIGHT)
            return !this.stack.isEmpty() && entity.getMainHandItem() == this.stack;
        else return !this.stack.isEmpty() && entity.getOffhandItem() == this.stack;
    }

    private static void handTick(LivingEntity entity) {
        if (ModSyncedDataKeys.MELEE_RIGHT.getValue(entity)) {
            handTick(entity, HumanoidArm.RIGHT);
        }
        else if (ModSyncedDataKeys.MELEE_LEFT.getValue(entity)) {
            handTick(entity, HumanoidArm.LEFT);
        }
        else if (TRACKER_MAP.containsKey(entity)) {
            TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity shooter, HumanoidArm arm) {
        if (addTracker(shooter, arm)) return;

        var tracker = TRACKER_MAP.get(shooter);
        var data = new GunData(tracker.stack, shooter);
        var isSameWeapon = !tracker.isSameWeapon(shooter);

        if (isSameWeapon || (!tracker.isEnd)) {
            TRACKER_MAP.remove(shooter);
            var reloadKey = getDataKey(arm);
            reloadKey.setValue(shooter, false);
        }

        if(tracker.meleeTick > 0)
            tracker.meleeTick--;

        if(tracker.meleeTick == tracker.attackDelay){
            tracker.tryMeleeAttack(shooter, tracker.stack);
        }

        if(tracker.meleeTick == 0){
            stopMelee(shooter, arm);
        }
    }

    // Метод ближней атаки
    private boolean tryMeleeAttack(LivingEntity shooter, ItemStack stack) {
        // Параметры атаки
        double reach = 3.0; // Дистанция атаки
        float damage = 6.0F; // Урон
        float knockback = 0.4F; // Отбрасывание
        int maxTargets = 1;

        AABB area = shooter.getBoundingBox().inflate(reach);
        List<Entity> entities = shooter.level().getEntities(shooter, area);

        // Фильтруем только атакуемые цели
        List<LivingEntity> targets = new ArrayList<>();
        for(Entity entity : entities) {
            if(entity instanceof LivingEntity living && entity.isAttackable() && !entity.isAlliedTo(shooter)) {
                targets.add(living);
            }
        }

        // Сортируем по расстоянию (ближайшие первые)
        targets.sort(Comparator.comparingDouble(e -> shooter.distanceToSqr(e)));

        // Ограничиваем количество целей если нужно
        if(maxTargets > 0 && targets.size() > maxTargets) {
            targets = targets.subList(0, maxTargets);
        }

        if(!targets.isEmpty()) {
            // Атакуем все подходящие цели
            for(LivingEntity target : targets) {
                performMeleeAttack(shooter, target, damage, knockback);
            }

            playAttackSound(shooter);
            spawnAttackParticles(shooter, targets);

            return true;
        }
        return false;
    }

    // Нанесение урона
    private void performMeleeAttack(LivingEntity shooter, Entity target, float damage, float knockback) {
        if(shooter instanceof Player player) {
            target.hurt(shooter.damageSources().playerAttack(player), damage);
        }
        else target.hurt(shooter.damageSources().mobAttack(shooter), damage);

        // Добавляем отбрасывание
        if(target instanceof LivingEntity livingTarget) {
            livingTarget.knockback(
                    knockback,
                    shooter.getX() - target.getX(),
                    shooter.getZ() - target.getZ()
            );
        }

        shooter.level().playSound(
                null,
                shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG,
                SoundSource.PLAYERS,
                0.8F,
                0.9F + shooter.getRandom().nextFloat() * 0.2F
        );
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

    private void spawnAttackParticles(LivingEntity shooter, List<LivingEntity> targets) {
        if(shooter.level() instanceof ServerLevel serverLevel) {
            // Эффекты вокруг игрока
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    shooter.getX(),
                    shooter.getY() + 1.0,
                    shooter.getZ(),
                    5,
                    0.5, 0.5, 0.5,
                    0.0
            );

            // Эффекты на каждой цели
            for(LivingEntity target : targets) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        target.getX(),
                        target.getY() + target.getBbHeight() / 2,
                        target.getZ(),
                        8,
                        0.1, 0.1, 0.1,
                        0.2
                );
            }
        }
    }

    private static void resetTracker(MeleeTracker tracker, GunData data) {
        tracker.meleeTick = GunModifierHelper.getReloadTime(data);
    }

    public static void startReloading(LivingEntity entity, HumanoidArm arm){
        var reloadKey = getDataKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static SyncedDataKey<LivingEntity, Boolean> getDataKey(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.MELEE_RIGHT : ModSyncedDataKeys.MELEE_LEFT;
    }

    private static boolean addTracker(LivingEntity entity, HumanoidArm arm) {
        var reloadKey = getDataKey(arm);

        var gunItem = arm == HumanoidArm.RIGHT ?
                entity.getMainHandItem().getItem():
                entity.getOffhandItem().getItem();

        if (!TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof GunItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            TRACKER_MAP.put(entity, new MeleeTracker(entity, arm));
        }
        return false;
    }

    private static void stopMelee(LivingEntity entity, HumanoidArm arm) {
        var dataKey = getDataKey(arm);

        TRACKER_MAP.remove(entity);
        dataKey.setValue(entity, false);
    }
}

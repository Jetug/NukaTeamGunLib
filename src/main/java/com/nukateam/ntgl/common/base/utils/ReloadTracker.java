package com.nukateam.ntgl.common.base.utils;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.DelayedTask;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.base.holders.LoadingType;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.network.message.S2CMessageGunSound;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageReload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.WeakHashMap;

import static com.nukateam.ntgl.common.util.util.LivingEntityUtils.getInteractionHand;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ReloadTracker {
    private static final Map<LivingEntity, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final LivingEntity shooter;
    private int slot = 0;
    private final HumanoidArm arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final Gun gun;

    public int reloadTick;
    public boolean isStart = false;
    public boolean isEnd = false;

    private ReloadTracker(LivingEntity entity, HumanoidArm arm) {
        this.startTick = entity.tickCount;
        this.arm = arm;
        this.stack = entity.getItemInHand(getInteractionHand(arm));
        this.gunItem = ((GunItem) stack.getItem());
        this.gun = gunItem.getModifiedGun(stack);
        this.shooter = entity;

        if(entity instanceof Player player) {
            this.slot = arm == HumanoidArm.RIGHT ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        }

        var data = new GunData(stack, entity);
        reloadTick = GunModifierHelper.getReloadTime(data);

        var loadingType = GunModifierHelper.getLoadingType(data);
        if(loadingType == LoadingType.PER_CARTRIDGE){
            ModSyncedDataKeys.RELOAD_START.setValue(entity, true);
            reloadTick = GunModifierHelper.getReloadStart(data);
            isStart = true;
        }

//        playReloadSound(entity);
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
                for (var entity: RELOAD_TRACKER_MAP.keySet()) {
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
            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getEntity()));
        }
    }

    /**
     * Tests if the current item the player is holding is the same as the one being reloaded
     *
     * @param entity the entity to check
     * @return True if it's the same weapon and slot
     */
    private boolean isSameWeapon(LivingEntity entity) {
        if(arm == HumanoidArm.RIGHT)
            return !this.stack.isEmpty() && entity.getMainHandItem() == this.stack;
        else return !this.stack.isEmpty() && entity.getOffhandItem() == this.stack;
    }

    private boolean isWeaponFull() {
        var data = new GunData(stack, shooter);
        return Gun.getAmmo(stack) >= GunEnchantmentHelper.getAmmoCapacity(data);
    }

    private boolean hasNoAmmo(LivingEntity player) {
        return Gun.hasNoAmmo(player, stack);
    }

    private boolean canReload(Player player) {
        int deltaTicks = player.tickCount - this.startTick;
        int interval = GunEnchantmentHelper.getReloadInterval(this.stack);
        return deltaTicks > 0 && deltaTicks % interval == 0;
    }

    private static void addOrDropStack(Player player, ItemStack usedMagazine) {
        if(!player.addItem(usedMagazine)){
            player.drop(usedMagazine, false);
        }
    }

//    private void playReloadSound(Player player) {
//        var reloadSound = this.gun.getSounds().getReload();
//        if (reloadSound != null) {
//            var pos = player.position().add(0, 1, 0);
//            var radius = Config.SERVER.reloadMaxDistance.get();
//            var message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, pos,
//                    1.0F, 1.0F, player.getId(), false, true);
//            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() ->
//                    new PacketDistributor.TargetPoint(
//                            player.getX(), (player.getY() + 1.0), player.getZ(), radius, player.level.dimension())), message);
//        }
//    }

    private static void handTick(LivingEntity entity) {
        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(entity)) {
            handTick(entity, HumanoidArm.RIGHT);
        }
        else if (ModSyncedDataKeys.RELOADING_LEFT.getValue(entity)) {
            handTick(entity, HumanoidArm.LEFT);
        }
        else if (RELOAD_TRACKER_MAP.containsKey(entity)) {
            RELOAD_TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity shooter, HumanoidArm arm) {
        if (addTracker(shooter, arm)) return;
        var tracker = RELOAD_TRACKER_MAP.get(shooter);
        var data = new GunData(tracker.stack, shooter);
        var loadingType = GunModifierHelper.getLoadingType(data);
        final var gun = tracker.gun;
        var isSameWeapon = !tracker.isSameWeapon(shooter);
        var isWeaponFull = tracker.isWeaponFull();
        var hasNoAmmo    = tracker.hasNoAmmo(shooter);

        if (isSameWeapon || (!tracker.isEnd && (isWeaponFull || hasNoAmmo))) {
            RELOAD_TRACKER_MAP.remove(shooter);
            var reloadKey = getReloadKey(arm);
            reloadKey.setValue(shooter, false);
        }
        else if(loadingType == LoadingType.MAGAZINE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                tracker.reloadMagazine(shooter);
                stopReloading(shooter, gun, arm);
            }
        }
        else if(loadingType == LoadingType.PER_CARTRIDGE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                if(tracker.isStart){
                    resetTracker(tracker, data);
                    tracker.isStart = false;
                    ModSyncedDataKeys.RELOAD_START.setValue(shooter, false);
                }
                else{
                    tracker.addCartridge(shooter);
                    if (tracker.isWeaponFull() || tracker.hasNoAmmo(shooter)) {
                        if(tracker.isEnd) {
                            ModSyncedDataKeys.RELOAD_END.setValue(shooter, false);
                            stopReloading(shooter, gun, arm);
                        }
                        else {
                            tracker.isEnd = true;
                            ModSyncedDataKeys.RELOAD_END.setValue(shooter, true);
                            tracker.reloadTick = GunModifierHelper.getReloadEnd(data);
                        }
                    }
                    else resetTracker(tracker, data);
                }
            }
        }
    }


    private void reloadMagazine(LivingEntity player) {
        var data = new GunData(stack, player);

        if(GunModifierHelper.getCurrentAmmo(data).isMagazineMode()){
            addMagazine(player);
        }
        else{
//            var amount = this.gun.getGeneral().getMaxAmmo(stack);
            addAmmo(player);
        }
    }

    private void addCartridge(LivingEntity entity) {
        addAmmo(entity, gun.getGeneral().getReloadAmount());
    }

    private void addAmmo(LivingEntity entity) {
        var data = new GunData(stack, entity);
        var amount = GunModifierHelper.getMaxAmmo(data);

        while (isNotReloaded(entity)){
            addAmmo(entity, amount);
        }
    }

    private void addAmmo(LivingEntity entity, int amount) {
        var context = Gun.findAmmo(entity, stack);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var tag = this.stack.getTag();
            amount = Math.min(ammo.getCount(), amount);

            if (tag != null) {
                var data = new GunData(stack, shooter);

                int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(data);
                amount = Math.min(amount, maxAmmo - tag.getInt(Tags.AMMO_COUNT));
                tag.putInt(Tags.AMMO_COUNT, tag.getInt(Tags.AMMO_COUNT) + amount);
            }
            ammo.shrink(amount);

            // Trigger that the container changed
            var container = context.container();
            if (container != null) {
                container.setChanged();
            }
        }
//        playReloadSound(player);
    }

    private boolean isNotReloaded(LivingEntity entity) {
        var data = new GunData(stack, entity);
        var ammoItem = GunModifierHelper.getCurrentAmmoId(data);
        var tag = this.stack.getTag();

        return !Gun.findAmmo(entity, stack).stack().isEmpty() &&
                tag.getInt(Tags.AMMO_COUNT) < GunEnchantmentHelper.getAmmoCapacity(data);
    }

    private void addMagazine(LivingEntity entity) {
        var data = new GunData(stack, entity);
        var ammoId = GunModifierHelper.getCurrentAmmoId(data);
        var context = Gun.findMagazine(entity, stack);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var amount = StackUtils.getDurability(ammo);
            var tag = this.stack.getTag();
//            amount = Math.min(this.gun.getGeneral().getMaxAmmo(stack), amount);
            amount = Math.min(GunModifierHelper.getMaxAmmo(data), amount);

            if (tag != null) {
                var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(data);
                var currentAmmo = tag.getInt(Tags.AMMO_COUNT);

                if(currentAmmo > 0) {
                    var usedMagazine = new ItemStack(ForgeRegistries.ITEMS.getValue(ammoId));
                    StackUtils.setDurability(usedMagazine, currentAmmo);

                    if(entity instanceof Player player)
                        addOrDropStack(player, usedMagazine);
                }
//                amount = Math.min(amount, maxAmmo - currentAmmo);
                tag.putInt(Tags.AMMO_COUNT, amount);
            }

            ammo.shrink(1);

            // Trigger that the container changed
            var container = context.container();
            if (container != null) {
                container.setChanged();
            }
        }
//        playReloadSound(player);
    }

    private static void resetTracker(ReloadTracker tracker, GunData data) {
        tracker.reloadTick = GunModifierHelper.getReloadTime(data);
    }

    public static void startReloading(LivingEntity entity, HumanoidArm arm){
        var reloadKey = getReloadKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static SyncedDataKey<LivingEntity, Boolean> getReloadKey(HumanoidArm arm) {
        var reloadKey = arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
        return reloadKey;
    }

    private static boolean addTracker(LivingEntity entity, HumanoidArm arm) {
        var reloadKey = getReloadKey(arm);

        var gunItem = arm == HumanoidArm.RIGHT ?
                entity.getMainHandItem().getItem():
                entity.getOffhandItem().getItem();

        if (!RELOAD_TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof GunItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            RELOAD_TRACKER_MAP.put(entity, new ReloadTracker(entity, arm));
        }
        return false;
    }

    private static void stopReloading(LivingEntity entity, Gun gun, HumanoidArm arm) {
        var reloadKey = getReloadKey(arm);

        RELOAD_TRACKER_MAP.remove(entity);
        reloadKey.setValue(entity, false);
        final var finalPlayer = entity;
        DelayedTask.runAfter(4, () -> gun.playCockSound(finalPlayer));
        var oppositeStack = LivingEntityUtils.getItemInHand(entity, arm.getOpposite());
        var data = new GunData(oppositeStack, entity);

        if (arm == HumanoidArm.RIGHT && oppositeStack.getItem() instanceof GunItem && !GunModifierHelper.isWeaponFull(data)) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) entity, new S2CMessageReload(true, arm.getOpposite()));
        }
    }
}

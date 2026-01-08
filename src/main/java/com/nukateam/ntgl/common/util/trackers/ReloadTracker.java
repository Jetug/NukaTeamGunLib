package com.nukateam.ntgl.common.util.trackers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.event.GunReloadEvent;
import com.nukateam.ntgl.common.data.holders.LoadingType;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ReloadTracker {
    private static final Map<LivingEntity, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final LivingEntity wielder;
    private final WeaponData data;
    private int slot = 0;
    private final InteractionHand arm;
    private final ItemStack weapon;
    private final WeaponConfig weaponConfig;

    public int reloadTick;
    public boolean isStart = false;
    public boolean isEnd = false;

    private ReloadTracker(WeaponData data, InteractionHand arm) {
        this.data = data;
        this.wielder = data.wielder;
        this.startTick = wielder.tickCount;
        this.arm = arm;
        this.weapon = data.weapon;
        var weaponItem = (IWeapon)weapon.getItem();
        this.weaponConfig = weaponItem.getModifiedConfig(weapon);

        if(wielder instanceof Player player) {
            this.slot = arm == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        }

        reloadTick = WeaponModifierHelper.getReloadTime(data);

        var loadingType = WeaponModifierHelper.getLoadingType(data);
        if(loadingType == LoadingType.PER_CARTRIDGE){
            ModSyncedDataKeys.RELOAD_START.setValue(wielder, true);
            reloadTick = WeaponModifierHelper.getReloadStart(data);
            isStart = true;
        }
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
        if(arm == InteractionHand.MAIN_HAND)
            return !this.weapon.isEmpty() && entity.getMainHandItem() == this.weapon;
        else return !this.weapon.isEmpty() && entity.getOffhandItem() == this.weapon;
    }

    private boolean isWeaponFull() {
        var data = new WeaponData(weapon, wielder);
        return WeaponStateHelper.getAmmoCount(data) >= WeaponModifierHelper.getMaxAmmo(data);
    }

    private boolean hasNoAmmo(LivingEntity wielder) {
        return !InventoryUtil.hasAmmo(new WeaponData(weapon, wielder));
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
            handTick(entity, InteractionHand.MAIN_HAND);
        }
        else if (ModSyncedDataKeys.RELOADING_LEFT.getValue(entity)) {
            handTick(entity, InteractionHand.OFF_HAND);
        }
        else if (RELOAD_TRACKER_MAP.containsKey(entity)) {
            RELOAD_TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity wielder, InteractionHand arm) {
        if (addTracker(new WeaponData(wielder.getItemInHand(arm), wielder), arm)) return;
        var tracker = RELOAD_TRACKER_MAP.get(wielder);
        var data = new WeaponData(tracker.weapon, wielder);
        var loadingType = WeaponModifierHelper.getLoadingType(data);
        var isSameWeapon = !tracker.isSameWeapon(wielder);
        var isWeaponFull = tracker.isWeaponFull();
        var hasNoAmmo    = tracker.hasNoAmmo(wielder);

        if (isSameWeapon || (!tracker.isEnd && (isWeaponFull || hasNoAmmo))) {
            RELOAD_TRACKER_MAP.remove(wielder);
            var reloadKey = ModSyncedDataKeys.getReloadKey(arm);
            reloadKey.setValue(wielder, false);
        }
        else if(loadingType == LoadingType.MAGAZINE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                tracker.reloadMagazine(wielder);
                stopReloading(wielder, arm);
            }
        }
        else if(loadingType == LoadingType.PER_CARTRIDGE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                if(tracker.isStart){
                    resetTracker(tracker, data);
                    tracker.isStart = false;
                    ModSyncedDataKeys.RELOAD_START.setValue(wielder, false);
                }
                else{
                    tracker.addCartridge(wielder);
                    if (tracker.isWeaponFull() || tracker.hasNoAmmo(wielder)) {
                        if(tracker.isEnd) {
                            ModSyncedDataKeys.RELOAD_END.setValue(wielder, false);
                            stopReloading(wielder, arm);
                        }
                        else {
                            tracker.isEnd = true;
                            ModSyncedDataKeys.RELOAD_END.setValue(wielder, true);
                            tracker.reloadTick = WeaponModifierHelper.getReloadEnd(data);
                        }
                    }
                    else resetTracker(tracker, data);
                }
            }
        }
    }

    private void reloadMagazine(LivingEntity entity) {
        var data = new WeaponData(weapon, entity);

        if(entity instanceof Player player && !player.isCreative()) {
            if(WeaponStateHelper.getProjectileConfig(data).isMagazineMode()){
                addMagazine(entity);
            }
            else{
                addAmmo(entity);
            }
        }
        else {
            WeaponStateHelper.setMaxAmmo(data);
        }
    }

    private void addCartridge(LivingEntity entity) {
        var gunData = new WeaponData(weapon, wielder);
        var reloadAmount = WeaponModifierHelper.getReloadAmount(gunData);

        if(entity instanceof Player player && !player.isCreative()) {
            addAmmo(entity, reloadAmount);
        }
        else {
            WeaponStateHelper.addAmmo(gunData, reloadAmount);
        }
    }

    private void addAmmo(LivingEntity entity) {
        var data = new WeaponData(weapon, entity);
        var amount = WeaponModifierHelper.getMaxAmmo(data);

        while (isNotReloaded(entity)){
            addAmmo(entity, amount);
        }
    }

    private void addAmmo(LivingEntity entity, int amount) {
        var data = new WeaponData(weapon, entity);
        var context = InventoryUtil.findAmmo(data);
        var ammo = context.stack();

        var ammoHandler = WeaponStateHelper.getCurrentAmmo(data);

        if (!ammo.isEmpty()) {
            var tag = this.weapon.getTag();
            var value = ammoHandler.getValue(ammo);
            var currentAmount = WeaponStateHelper.getAmmoCount(data);

            amount = Math.min(ammo.getCount() * value, amount);

            if (tag != null) {
                var gunData = new WeaponData(weapon, wielder);
                var maxAmmo = WeaponModifierHelper.getMaxAmmo(gunData);
                var ammoCount = WeaponStateHelper.getAmmoCount(data);
                amount = Math.min(amount, maxAmmo - ammoCount);
                WeaponStateHelper.addAmmo(gunData, amount);
            }

            context.shrink(amount, ammoHandler, entity);
        }
    }

    private boolean isNotReloaded(LivingEntity entity) {
        var data = new WeaponData(weapon, entity);
        var tag = this.weapon.getTag();
        var hasAmmo = InventoryUtil.hasAmmo(data);
        var ammoCount = WeaponStateHelper.getAmmoCount(data);
        var ammoCapacity = WeaponModifierHelper.getMaxAmmo(data);
        return hasAmmo && ammoCount < ammoCapacity;
    }

//    private boolean isNotReloaded(LivingEntity entity) {
//        var data = new GunData(weapon, entity);
//        var ammoItem = GunStateHelper.getAmmoId(data);
//        var tag = this.weapon.getTag();
//
//        return !InventoryUtil.findAmmo(entity, weapon).stack().isEmpty() &&
//                tag.getInt(Tags.AMMO_COUNT) < WeaponModifierHelper.getMaxAmmo(data);
//    }

    private void addMagazine(LivingEntity entity) {
        var data = new WeaponData(weapon, entity);
        var ammoHolder = WeaponStateHelper.getCurrentAmmo(data);
        var context = InventoryUtil.findMagazine(entity, weapon);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var amount = StackUtils.getDurability(ammo);
            var tag = this.weapon.getTag();
            amount = Math.min(WeaponModifierHelper.getMaxAmmo(data), amount);

            if (tag != null) {
                var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
                var currentAmmo = WeaponStateHelper.getAmmoCount(data);

                if(currentAmmo > 0 && ammoHolder.canReturnAmmo()) {
                    var usedMagazine = new ItemStack(ForgeRegistries.ITEMS.getValue(ammoHolder.getId()));
                    StackUtils.setDurability(usedMagazine, currentAmmo);

                    if(entity instanceof Player player)
                        addOrDropStack(player, usedMagazine);
                }
                WeaponStateHelper.setAmmoCount(data, amount);
            }
            context.shrink(1, ammoHolder, entity);
        }
    }

    private static void resetTracker(ReloadTracker tracker, WeaponData data) {
        tracker.reloadTick = WeaponModifierHelper.getReloadTime(data);
    }

    public static void startReloading(WeaponData data, InteractionHand arm){
        var reloadKey = ModSyncedDataKeys.getReloadKey(arm);
        reloadKey.setValue(data.wielder, true);
        addTracker(data, arm);
    }

    public static void stopReloading(LivingEntity wielder, InteractionHand hand) {
        var reloadKey = ModSyncedDataKeys.getReloadKey(hand);
        reloadKey.setValue(wielder, false);
        var tracker = RELOAD_TRACKER_MAP.get(wielder);

        if(tracker != null){
            var data = tracker.data;
            reloadSecondHand(wielder, hand, data);
            MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(data, hand));
            RELOAD_TRACKER_MAP.remove(wielder);
//            DelayedTask.runAfter(4, () -> gun.playCockSound(wielder));
        }
    }

    private static boolean addTracker(WeaponData data, InteractionHand arm) {
        var reloadKey = ModSyncedDataKeys.getReloadKey(arm);
        var entity = data.wielder;
        var weaponItem = data.weapon.getItem();

        if (!RELOAD_TRACKER_MAP.containsKey(entity)) {
            if (!(weaponItem instanceof IWeapon)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            RELOAD_TRACKER_MAP.put(entity, new ReloadTracker(data, arm));
        }
        return false;
    }

    private static void reloadSecondHand(LivingEntity wielder, InteractionHand hand, WeaponData data) {
        var oppositeHand = LivingEntityUtils.getOppositeHand(hand);
        var oppositeStack = wielder.getItemInHand(oppositeHand);

        if (hand == InteractionHand.MAIN_HAND
                && oppositeStack.getItem() instanceof IWeapon
                && !WeaponStateHelper.isWeaponFull(new WeaponData(oppositeStack, wielder))) {
            startReloading(data, InteractionHand.OFF_HAND);
        }
    }
}
